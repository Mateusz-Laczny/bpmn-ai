package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.AddSequenceOfTasksFunction;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
import edu.agh.bpmnai.generator.v2.functions.parameter.SequenceOfTasksDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class AddSequenceOfTasksCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    private final ActivityService activityService;

    @Autowired
    public AddSequenceOfTasksCallExecutor(ToolCallArgumentsParser callArgumentsParser, SessionStateStore sessionStateStore, ActivityService activityService) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
        this.activityService = activityService;
    }

    @Override
    public String getFunctionName() {
        return AddSequenceOfTasksFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, List<String>> executeCall(String callArgumentsJson) {
        Result<SequenceOfTasksDto, List<String>> argumentsParsingResult = callArgumentsParser.parseArguments(callArgumentsJson, SequenceOfTasksDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        SequenceOfTasksDto callArguments = argumentsParsingResult.getValue();
        BpmnModel model = sessionStateStore.model();
        Optional<String> optionalPredecessorElementId = model.findElementByModelFriendlyId(callArguments.startOfSequence());
        if (optionalPredecessorElementId.isEmpty()) {
            log.info("Predecessor element does not exist in the model");
            return Result.error(List.of("Predecessor element does not exist in the model"));
        }

        String predecessorElementId = optionalPredecessorElementId.get();
        Set<String> predecessorTaskSuccessorsBeforeModification = model.findSuccessors(predecessorElementId);
        if (predecessorTaskSuccessorsBeforeModification.size() > 1) {
            log.warn("Predecessor activity has more than one successor, choosing the first one; activityName: {}", callArguments.startOfSequence());
        }

        model.clearSuccessors(predecessorElementId);

        Set<String> addedActivitiesNames = new HashSet<>();
        for (Activity activityInSequence : callArguments.activitiesInSequence()) {
            Result<ActivityIdAndName, String> activityAddResult = activityService.addActivityToModel(model, activityInSequence);
            if (activityAddResult.isError()) {
                return Result.error(List.of(activityAddResult.getError()));
            }

            String activityId = activityAddResult.getValue().id();
            addedActivitiesNames.add(activityAddResult.getValue().modelFacingName());

            if (!model.areElementsDirectlyConnected(predecessorElementId, activityId)) {
                model.addUnlabelledSequenceFlow(predecessorElementId, activityId);
            }

            predecessorElementId = activityId;
        }

        if (!predecessorTaskSuccessorsBeforeModification.isEmpty()) {
            if (predecessorTaskSuccessorsBeforeModification.size() > 1) {
                log.warn("Predecessor element has more than one successor, choosing the first one; activityName: {}", callArguments.startOfSequence());
            }

            String endOfChainElementId = predecessorTaskSuccessorsBeforeModification.iterator().next();
            if (!model.areElementsDirectlyConnected(predecessorElementId, endOfChainElementId)) {
                model.addUnlabelledSequenceFlow(predecessorElementId, endOfChainElementId);
            }
        }

        return Result.ok("Added activities: " + addedActivitiesNames);
    }
}
