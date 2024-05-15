package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.NodeIdToModelInterfaceIdFunction;
import edu.agh.bpmnai.generator.v2.functions.AddWhileLoopFunction;
import edu.agh.bpmnai.generator.v2.functions.FindInsertionPointForSubprocessWithCheckTask;
import edu.agh.bpmnai.generator.v2.functions.InsertElementIntoDiagram;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.WhileLoopDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;

@Service
@Slf4j
public class AddWhileLoopCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    private final InsertElementIntoDiagram insertElementIntoDiagram;

    private final NodeIdToModelInterfaceIdFunction nodeIdToModelInterfaceIdFunction;

    private final FindInsertionPointForSubprocessWithCheckTask findInsertionPointForSubprocessWithCheckTask;

    @Autowired
    public AddWhileLoopCallExecutor(
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

        Result<FindInsertionPointForSubprocessWithCheckTask.InsertionPointFindResult, String> insertionPointFindResult =
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

        String gatewayId = model.addGateway(EXCLUSIVE, callArguments.subprocessName() + " gateway");
        addedNodesIds.add(gatewayId);

        String previousElementInLoopId = gatewayId;
        for (String taskInLoop : callArguments.tasksInLoop()) {
            String taskId = model.addTask(taskInLoop);
            addedNodesIds.add(taskId);

            if (!model.areElementsDirectlyConnected(previousElementInLoopId, taskId)) {
                model.addUnlabelledSequenceFlow(previousElementInLoopId, taskId);
            }

            previousElementInLoopId = taskId;
        }

        model.addUnlabelledSequenceFlow(previousElementInLoopId, insertionPointId);

        Result<Void, String> insertSubdiagramResult = insertElementIntoDiagram.apply(
                insertionPointId,
                gatewayId,
                gatewayId,
                model
        );

        if (insertSubdiagramResult.isError()) {
            return Result.error(insertSubdiagramResult.getError());
        }

        if (model.findSuccessors(gatewayId).size() == 1) {
            String endEventId = model.addEndEvent();
            model.addUnlabelledSequenceFlow(gatewayId, endEventId);
            addedNodesIds.add(endEventId);
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
                model.getName(gatewayId).orElseThrow(),
                sessionStateStore.getModelInterfaceId(gatewayId).orElseThrow()
        );

        return Result.ok("Call successful; subprocess start node: '%s', subprocess end node: '%s'".formatted(
                subprocessStartNode,
                subprocessEndNode
        ));
    }
}
