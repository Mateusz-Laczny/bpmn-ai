package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.AddSequenceOfTasksFunction;
import edu.agh.bpmnai.generator.v2.functions.InsertElementIntoDiagram;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
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

    private final ActivityService activityService;

    private final InsertElementIntoDiagram insertElementIntoDiagram;

    @Autowired
    public AddSequenceOfTasksCallExecutor(
            ToolCallArgumentsParser callArgumentsParser,
            SessionStateStore sessionStateStore,
            ActivityService activityService,
            InsertElementIntoDiagram insertElementIntoDiagram
    ) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
        this.activityService = activityService;
        this.insertElementIntoDiagram = insertElementIntoDiagram;
    }

    @Override
    public String getFunctionName() {
        return AddSequenceOfTasksFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson, BpmnManagedReference modelReference) {
        Result<SequenceOfTasksDto, String> argumentsParsingResult = callArgumentsParser.parseArguments(
                callArgumentsJson,
                SequenceOfTasksDto.class
        );
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        SequenceOfTasksDto callArguments = argumentsParsingResult.getValue();
        BpmnModel model = modelReference.getCurrentValue();
        Optional<String> optionalPredecessorElementId =
                model.findElementByModelFriendlyId(callArguments.startOfSequence());
        if (optionalPredecessorElementId.isEmpty()) {
            log.info("Predecessor element does not exist in the model");
            return Result.error("Predecessor element does not exist in the model");
        }

        String predecessorElementId = optionalPredecessorElementId.get();
        Set<String> addedActivitiesNames = new HashSet<>();
        String previousElementInSequenceId = null;
        for (Activity activityInSequence : callArguments.activitiesInSequence()) {
            Result<ActivityIdAndName, String> activityAddResult = activityService.addActivityToModel(
                    model,
                    activityInSequence
            );
            if (activityAddResult.isError()) {
                return Result.error(activityAddResult.getError());
            }

            String activityId = activityAddResult.getValue().id();
            addedActivitiesNames.add(activityAddResult.getValue().modelFacingName());

            if (previousElementInSequenceId != null && !model.areElementsDirectlyConnected(
                    previousElementInSequenceId,
                    activityId
            )) {
                model.addUnlabelledSequenceFlow(previousElementInSequenceId, activityId);
            }

            previousElementInSequenceId = activityId;
        }

        String sequenceStartElementId = model.findElementByModelFriendlyId(callArguments.activitiesInSequence()
                                                                                   .get(0)
                                                                                   .activityName()).orElseThrow();
        String sequenceEndElementId = previousElementInSequenceId;

        Result<Void, String> insertElementResult = insertElementIntoDiagram.apply(
                predecessorElementId,
                sequenceStartElementId,
                sequenceEndElementId,
                model
        );

        if (insertElementResult.isError()) {
            return Result.error(insertElementResult.getError());
        }

        modelReference.setValue(model);

        return Result.ok("Added activities: " + addedActivitiesNames);
    }
}
