package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.NodeIdToModelInterfaceIdFunction;
import edu.agh.bpmnai.generator.v2.functions.AddXorGatewayFunction;
import edu.agh.bpmnai.generator.v2.functions.FindInsertionPointForSubprocessWithCheckTask;
import edu.agh.bpmnai.generator.v2.functions.FindInsertionPointForSubprocessWithCheckTask.InsertionPointFindResult;
import edu.agh.bpmnai.generator.v2.functions.InsertElementIntoDiagram;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.Task;
import edu.agh.bpmnai.generator.v2.functions.parameter.XorGatewayDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;

@Service
@Slf4j
public class AddXorGatewayCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    private final InsertElementIntoDiagram insertElementIntoDiagram;

    private final NodeIdToModelInterfaceIdFunction nodeIdToModelInterfaceIdFunction;

    private final FindInsertionPointForSubprocessWithCheckTask findInsertionPointForSubprocessWithCheckTask;

    @Autowired
    public AddXorGatewayCallExecutor(
            ToolCallArgumentsParser callArgumentsParser,
            SessionStateStore sessionStateStore,
            InsertElementIntoDiagram insertElementIntoDiagram,
            NodeIdToModelInterfaceIdFunction nodeIdToModelInterfaceIdFunction,
            FindInsertionPointForSubprocessWithCheckTask findInsertionPointForSubprocessWithCheckTask
    ) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
        this.insertElementIntoDiagram = insertElementIntoDiagram;
        this.nodeIdToModelInterfaceIdFunction = nodeIdToModelInterfaceIdFunction;
        this.findInsertionPointForSubprocessWithCheckTask = findInsertionPointForSubprocessWithCheckTask;
    }

    @Override
    public String getFunctionName() {
        return AddXorGatewayFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson) {
        Result<XorGatewayDto, String> argumentsParsingResult = callArgumentsParser.parseArguments(
                callArgumentsJson,
                XorGatewayDto.class
        );
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        XorGatewayDto callArguments = argumentsParsingResult.getValue();

        if (callArguments.tasksInsideGateway().size() < 2) {
            return Result.error("A gateway must contain at least 2 activities");
        }

        Result<InsertionPointFindResult, String> insertionPointFindResult =
                findInsertionPointForSubprocessWithCheckTask.apply(
                        callArguments.checkTask(),
                        callArguments.insertionPoint()
                );
        if (insertionPointFindResult.isError()) {
            return Result.error(insertionPointFindResult.getError());
        }

        String insertionPointId = insertionPointFindResult.getValue().insertionPointId();
        Set<String> addedNodesIds = new HashSet<>();
        if (insertionPointFindResult.getValue().isANewTask()) {
            addedNodesIds.add(insertionPointId);
        }

        BpmnModel model = sessionStateStore.model();

        String openingGatewayId = model.addGateway(EXCLUSIVE, callArguments.subprocessName() + " opening gateway");
        addedNodesIds.add(openingGatewayId);
        String closingGatewayId = model.addGateway(EXCLUSIVE, callArguments.subprocessName() + " closing gateway");
        addedNodesIds.add(closingGatewayId);

        for (Task taskInGateway : callArguments.tasksInsideGateway()) {
            String taskId = model.addTask(taskInGateway.taskName());
            addedNodesIds.add(taskId);
            model.addUnlabelledSequenceFlow(openingGatewayId, taskId);
            if (taskInGateway.isProcessEnd()) {
                String endEventId = model.addEndEvent();
                addedNodesIds.add(endEventId);
                model.addUnlabelledSequenceFlow(taskId, endEventId);
            } else {
                model.addUnlabelledSequenceFlow(taskId, closingGatewayId);
            }
        }

        String subdiagramEndElement = closingGatewayId;
        Set<String> closingGatewayPredecessors = model.findPredecessors(closingGatewayId);
        if (closingGatewayPredecessors.size() == 1) {
            // A gateway with a single predecessor is useless, so just remove it
            model.removeFlowNode(closingGatewayId);
            addedNodesIds.remove(closingGatewayId);
            subdiagramEndElement = closingGatewayPredecessors.iterator().next();
        }

        Result<Void, String> elementInsertResult = insertElementIntoDiagram.apply(
                insertionPointId,
                openingGatewayId,
                subdiagramEndElement,
                model
        );

        if (elementInsertResult.isError()) {
            return Result.error(elementInsertResult.getError());
        }

        sessionStateStore.setModel(model);
        for (String nodeId : addedNodesIds) {
            sessionStateStore.setModelInterfaceId(nodeId, nodeIdToModelInterfaceIdFunction.apply(nodeId));
        }

        HumanReadableId subprocessStartNode = new HumanReadableId(
                model.getName(insertionPointId).orElseThrow(),
                sessionStateStore.getModelInterfaceId(insertionPointId).orElseThrow()
        );
        HumanReadableId subprocessEndNode = new HumanReadableId(
                model.getName(subdiagramEndElement).orElseThrow(),
                sessionStateStore.getModelInterfaceId(subdiagramEndElement).orElseThrow()
        );

        return Result.ok("Call successful; subprocess start node: '%s', subprocess end node: '%s'".formatted(
                subprocessStartNode,
                subprocessEndNode
        ));
    }
}
