package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.parameter.SequenceOfActivitiesDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
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

    @Autowired
    public AddSequenceOfTasksCallExecutor(ToolCallArgumentsParser callArgumentsParser) {
        this.callArgumentsParser = callArgumentsParser;
    }

    @Override
    public String getFunctionName() {
        return "add_sequence_of_activities";
    }

    @Override
    public FunctionCallResult executeCall(SessionState sessionState, String functionId, String callArgumentsJson) {
        ArgumentsParsingResult<SequenceOfActivitiesDto> argumentsParsingResult = callArgumentsParser.parseArguments(callArgumentsJson, SequenceOfActivitiesDto.class);
        if (argumentsParsingResult.isError()) {
            return FunctionCallResult.unsuccessfulCall(argumentsParsingResult.errors());
        }

        SequenceOfActivitiesDto callArguments = argumentsParsingResult.result();
        BpmnModel model = sessionState.model();
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

        for (String newActivityName : callArguments.newActivities()) {
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
