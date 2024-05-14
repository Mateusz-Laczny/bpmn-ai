package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.NodeIdToModelInterfaceIdFunction;
import edu.agh.bpmnai.generator.v2.functions.AddWhileLoopFunction;
import edu.agh.bpmnai.generator.v2.functions.InsertElementIntoDiagram;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.WhileLoopDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;
import static edu.agh.bpmnai.generator.bpmn.model.HumanReadableId.isHumanReadableIdentifier;

@Service
@Slf4j
public class AddWhileLoopCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    private final InsertElementIntoDiagram insertElementIntoDiagram;

    private final NodeIdToModelInterfaceIdFunction nodeIdToModelInterfaceIdFunction;

    @Autowired
    public AddWhileLoopCallExecutor(
            ToolCallArgumentsParser callArgumentsParser,
            SessionStateStore sessionStateStore,
            InsertElementIntoDiagram insertElementIntoDiagram,
            NodeIdToModelInterfaceIdFunction nodeIdToModelInterfaceIdFunction
    ) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
        this.insertElementIntoDiagram = insertElementIntoDiagram;
        this.nodeIdToModelInterfaceIdFunction = nodeIdToModelInterfaceIdFunction;
    }

    @Override
    public String getFunctionName() {
        return AddWhileLoopFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson) {
        Result<WhileLoopDto, String> argumentsParsingResult = callArgumentsParser.parseArguments(
                callArgumentsJson,
                WhileLoopDto.class
        );
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        WhileLoopDto callArguments = argumentsParsingResult.getValue();

        BpmnModel model = sessionStateStore.model();
        Set<String> addedNodesIds = new HashSet<>();

        String checkTaskId;
        boolean checkTaskExistsInTheModel;
        if (isHumanReadableIdentifier(callArguments.checkTask())) {
            String checkTaskModelInterfaceId = HumanReadableId.fromString(callArguments.checkTask()).id();
            Optional<String> checkTaskIdOptional = sessionStateStore.getElementId(checkTaskModelInterfaceId);
            if (checkTaskIdOptional.isEmpty()) {
                return Result.error("Check task '%s' does not exist in the diagram".formatted(callArguments.checkTask()));
            }

            checkTaskId = checkTaskIdOptional.get();
            checkTaskExistsInTheModel = true;
        } else {
            checkTaskId = model.addTask(callArguments.checkTask());
            addedNodesIds.add(checkTaskId);
            checkTaskExistsInTheModel = false;
        }

        String insertionPoint;
        String subdiagramStartNode = null;
        if (checkTaskExistsInTheModel) {
            insertionPoint = checkTaskId;
        } else {
            if (callArguments.insertionPoint() == null) {
                log.warn(
                        "Call unsuccessful, insertion point is null when check task '{}' does not exist in the "
                        + "diagram",
                        callArguments.checkTask()
                );
                return Result.error("Insertion point is null, when check task does not exist in the diagram");
            }

            if (!model.nodeIdExist(callArguments.insertionPoint().id())) {
                log.warn(
                        "Call unsuccessful, insertion point '{}' does not exist in the diagram",
                        callArguments.insertionPoint()
                );
                return Result.error("Insertion point '%s' does not exist in the diagram".formatted(callArguments.insertionPoint()));
            }

            insertionPoint = callArguments.insertionPoint().id();
            subdiagramStartNode = checkTaskId;
        }

        String gatewayId = model.addGateway(EXCLUSIVE, callArguments.subprocessName() + " gateway");
        addedNodesIds.add(gatewayId);
        if (subdiagramStartNode == null) {
            subdiagramStartNode = gatewayId;
        } else {
            model.addUnlabelledSequenceFlow(checkTaskId, gatewayId);
        }

        String previousElementInLoopId = gatewayId;
        for (String taskInLoop : callArguments.tasksInLoop()) {
            if (model.findElementByName(taskInLoop).isPresent()) {
                return Result.error("Node with name '%s' already exists in the diagram".formatted(taskInLoop));
            }

            String taskId = model.addTask(taskInLoop);
            addedNodesIds.add(taskId);

            if (!model.areElementsDirectlyConnected(previousElementInLoopId, taskId)) {
                model.addUnlabelledSequenceFlow(previousElementInLoopId, taskId);
            }

            previousElementInLoopId = taskId;
        }

        if (!model.areElementsDirectlyConnected(previousElementInLoopId, checkTaskId)) {
            model.addUnlabelledSequenceFlow(previousElementInLoopId, checkTaskId);
        }

        Result<Void, String> insertSubdiagramResult = insertElementIntoDiagram.apply(
                insertionPoint,
                subdiagramStartNode,
                gatewayId,
                model
        );

        if (insertSubdiagramResult.isError()) {
            return Result.error(insertSubdiagramResult.getError());
        }

        sessionStateStore.setModel(model);
        for (String nodeId : addedNodesIds) {
            sessionStateStore.setModelInterfaceId(nodeId, nodeIdToModelInterfaceIdFunction.apply(nodeId));
        }

        HumanReadableId subprocessStartNode = new HumanReadableId(
                model.getName(subdiagramStartNode).orElseThrow(),
                sessionStateStore.getModelInterfaceId(subdiagramStartNode).orElseThrow()
        );
        HumanReadableId subprocessEndNode = new HumanReadableId(
                model.getName(gatewayId).orElseThrow(),
                sessionStateStore.getModelInterfaceId(gatewayId).orElseThrow()
        );

        return Result.ok("Call successful; subprocess start node: '%s', subprocess end node: '%s'".formatted(
                subprocessStartNode,
                subprocessEndNode
        ));
    }
}
