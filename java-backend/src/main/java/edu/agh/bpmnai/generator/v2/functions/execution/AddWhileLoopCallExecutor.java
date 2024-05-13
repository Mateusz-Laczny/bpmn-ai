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
                return Result.error("Check task '%s' does not exist in the model".formatted(callArguments.checkTask()));
            }

            checkTaskId = checkTaskIdOptional.get();
            checkTaskExistsInTheModel = true;
        } else {
            checkTaskId = model.addTask(callArguments.checkTask());
            addedNodesIds.add(checkTaskId);
            checkTaskExistsInTheModel = false;
        }

        String subdiagramPredecessorElement;
        String subdiagramStartElement = null;
        if (checkTaskExistsInTheModel) {
            subdiagramPredecessorElement = checkTaskId;
        } else {
            if (callArguments.predecessorElement() == null) {
                log.warn(
                        "Call unsuccessful, predecessor element is null when check task '{}' does not exist in the "
                        + "model",
                        callArguments.checkTask()
                );
                return Result.error("Predecessor element is null, when check task does not exist in the model");
            }

            if (!model.doesIdExist(callArguments.predecessorElement().id())) {
                log.warn(
                        "Call unsuccessful, predecessor element '{}' does not exist in the model",
                        callArguments.predecessorElement()
                );
                return Result.error("Predecessor element %s does not exist in the model".formatted(callArguments.predecessorElement()));
            }

            subdiagramPredecessorElement = callArguments.predecessorElement().id();
            subdiagramStartElement = checkTaskId;
        }

        String gatewayId = model.addGateway(EXCLUSIVE, callArguments.elementName() + " gateway");
        addedNodesIds.add(gatewayId);
        if (subdiagramStartElement == null) {
            subdiagramStartElement = gatewayId;
        } else {
            model.addUnlabelledSequenceFlow(checkTaskId, gatewayId);
        }

        String previousElementInLoopId = gatewayId;
        for (String taskInLoop : callArguments.tasksInLoop()) {
            if (model.findElementByName(taskInLoop).isPresent()) {
                return Result.error("Element '%s' already exists in the model".formatted(taskInLoop));
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
                subdiagramPredecessorElement,
                subdiagramStartElement,
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

        return Result.ok("Call successful");
    }
}
