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
import java.util.Set;

@Service
@Slf4j
public class AddSequenceOfTasksCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    private final InsertElementIntoDiagram insertElementIntoDiagram;

    @Autowired
    public AddSequenceOfTasksCallExecutor(
            ToolCallArgumentsParser callArgumentsParser,
            SessionStateStore sessionStateStore,
            InsertElementIntoDiagram insertElementIntoDiagram
    ) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
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
        if (!model.doesIdExist(callArguments.startOfSequence().id())) {
            log.info("Predecessor element does not exist in the model");
            return Result.error("Predecessor element does not exist in the model");
        }

        String predecessorElementId = callArguments.startOfSequence().id();
        Set<String> addedActivitiesNames = new HashSet<>();
        String previousElementInSequenceId = null;
        for (Activity activityInSequence : callArguments.activitiesInSequence()) {
            if (model.findElementByName(activityInSequence.activityName()).isPresent()) {
                return Result.error("Element %s already exists in the model".formatted(activityInSequence.activityName()));
            }

            String activityId = model.addTask(activityInSequence.activityName());
            addedActivitiesNames.add(activityInSequence.activityName());

            if (previousElementInSequenceId != null && !model.areElementsDirectlyConnected(
                    previousElementInSequenceId,
                    activityId
            )) {
                model.addUnlabelledSequenceFlow(previousElementInSequenceId, activityId);
            }

            previousElementInSequenceId = activityId;
        }

        String sequenceStartElementId = model.findElementByName(callArguments.activitiesInSequence()
                                                                        .get(0)
                                                                        .activityName()).orElseThrow();
        String lastElementInSequenceId = previousElementInSequenceId;
        String sequenceEndElementId = null;
        if (!callArguments.activitiesInSequence().get(callArguments.activitiesInSequence().size() - 1).isProcessEnd()) {
            sequenceEndElementId = lastElementInSequenceId;
        } else {
            String endEventId = model.addEndEvent();
            model.addUnlabelledSequenceFlow(lastElementInSequenceId, endEventId);
        }

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
