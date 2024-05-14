package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.NodeIdToModelInterfaceIdFunction;
import edu.agh.bpmnai.generator.v2.functions.AddSequenceOfTasksFunction;
import edu.agh.bpmnai.generator.v2.functions.InsertElementIntoDiagram;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.SequenceOfTasksDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class AddSequenceOfTasksCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    private final InsertElementIntoDiagram insertElementIntoDiagram;

    private final NodeIdToModelInterfaceIdFunction nodeIdToModelInterfaceIdFunction;

    @Autowired
    public AddSequenceOfTasksCallExecutor(
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
        return AddSequenceOfTasksFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson) {
        Result<SequenceOfTasksDto, String> argumentsParsingResult =
                callArgumentsParser.parseArguments(callArgumentsJson, SequenceOfTasksDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        SequenceOfTasksDto callArguments = argumentsParsingResult.getValue();
        BpmnModel model = sessionStateStore.model();
        Optional<String> startOfSequenceNodeId = sessionStateStore.getElementId(callArguments.insertionPoint().id());
        if (startOfSequenceNodeId.isEmpty()) {
            log.info("Insertion point '{}' does not exist in the diagram", callArguments.insertionPoint().asString());
            return Result.error("Insertion point '%s' doesn't exist in the diagram".formatted(callArguments.insertionPoint()
                                                                                                      .asString()));
        }

        String predecessorElementId = startOfSequenceNodeId.get();
        Set<String> addedTasks = new HashSet<>();
        String previousElementInSequenceId = null;
        for (String taskInSequence : callArguments.tasksInSequence()) {
            Optional<String> elementId = model.findElementByName(taskInSequence);
            if (elementId.isPresent()) {
                return Result.error("Node with name '%s' already exists in the diagram".formatted(model.getHumanReadableId(
                        elementId.get()).orElseThrow().asString()));
            }

            String taskId = model.addTask(taskInSequence);
            addedTasks.add(taskId);

            if (previousElementInSequenceId != null && !model.areElementsDirectlyConnected(
                    previousElementInSequenceId,
                    taskId
            )) {
                model.addUnlabelledSequenceFlow(previousElementInSequenceId, taskId);
            }

            previousElementInSequenceId = taskId;
        }

        String sequenceStartElementId = model.findElementByName(callArguments.tasksInSequence().get(0)).orElseThrow();
        String lastElementInSequenceId = previousElementInSequenceId;

        Result<Void, String> insertElementResult = insertElementIntoDiagram.apply(
                predecessorElementId,
                sequenceStartElementId,
                lastElementInSequenceId,
                model
        );

        if (insertElementResult.isError()) {
            return Result.error(insertElementResult.getError());
        }

        sessionStateStore.setModel(model);

        for (String taskId : addedTasks) {
            sessionStateStore.setModelInterfaceId(taskId, nodeIdToModelInterfaceIdFunction.apply(taskId));
        }

        HumanReadableId subprocessStartNode = new HumanReadableId(
                model.getName(sequenceStartElementId).orElseThrow(),
                sessionStateStore.getModelInterfaceId(
                        sequenceStartElementId).orElseThrow()
        );
        HumanReadableId subprocessEndNode = new HumanReadableId(
                model.getName(lastElementInSequenceId).orElseThrow(),
                sessionStateStore.getModelInterfaceId(
                        lastElementInSequenceId).orElseThrow()
        );

        return Result.ok("Call successful; subprocess start node: '%s', subprocess end node: '%s'".formatted(
                subprocessStartNode,
                subprocessEndNode
        ));
    }
}
