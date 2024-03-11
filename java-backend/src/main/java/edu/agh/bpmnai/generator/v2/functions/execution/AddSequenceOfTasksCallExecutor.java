package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.AddSequenceOfTasksFunction;
import edu.agh.bpmnai.generator.v2.functions.ArgumentsParsingResult;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.SequenceOfTasksDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class AddSequenceOfTasksCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    @Autowired
    public AddSequenceOfTasksCallExecutor(ToolCallArgumentsParser callArgumentsParser, SessionStateStore sessionStateStore) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
    }

    @Override
    public String getFunctionName() {
        return AddSequenceOfTasksFunction.FUNCTION_NAME;
    }

    @Override
    public FunctionCallResult executeCall(String callArgumentsJson) {
        ArgumentsParsingResult<SequenceOfTasksDto> argumentsParsingResult = callArgumentsParser.parseArguments(callArgumentsJson, SequenceOfTasksDto.class);
        if (argumentsParsingResult.isError()) {
            return FunctionCallResult.unsuccessfulCall(argumentsParsingResult.errors());
        }

        SequenceOfTasksDto callArguments = argumentsParsingResult.result();
        BpmnModel model = sessionStateStore.model();
        Optional<String> optionalPredecessorElementId = model.findTaskIdByName(callArguments.predecessorElement());
        if (optionalPredecessorElementId.isEmpty()) {
            log.info("Predecessor element does not exist in the model");
            return FunctionCallResult.unsuccessfulCall(List.of("Predecessor element does not exist in the model"));
        }

        String predecessorElementId = optionalPredecessorElementId.get();

        model.clearSuccessors(predecessorElementId);

        Set<String> predecessorTaskSuccessorsBeforeModification = model.findSuccessors(predecessorElementId);
        if (predecessorTaskSuccessorsBeforeModification.size() > 1) {
            log.warn("Predecessor activity has more than one successor, choosing the first one; activityName: {}", callArguments.predecessorElement());
        }

        for (String newActivityName : callArguments.tasksInSequence()) {
            String nextTaskId = model.findTaskIdByName(newActivityName).orElseGet(() -> model.addTask(newActivityName));
            if (model.findSuccessors(predecessorElementId).contains(nextTaskId)) {
                continue;
            }

            model.addUnlabelledSequenceFlow(predecessorElementId, nextTaskId);
            predecessorElementId = nextTaskId;
        }

        if (!predecessorTaskSuccessorsBeforeModification.isEmpty()) {
            if (predecessorTaskSuccessorsBeforeModification.size() > 1) {
                log.warn("Predecessor element has more than one successor, choosing the first one; activityName: {}", callArguments.predecessorElement());
            }

            String endOfChainElementId = predecessorTaskSuccessorsBeforeModification.iterator().next();
            model.addUnlabelledSequenceFlow(predecessorElementId, endOfChainElementId);
        }

        return FunctionCallResult.successfulCall();
    }
}
