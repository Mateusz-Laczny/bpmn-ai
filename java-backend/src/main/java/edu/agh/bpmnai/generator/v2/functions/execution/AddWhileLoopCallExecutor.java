package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.AddWhileLoopFunction;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
import edu.agh.bpmnai.generator.v2.functions.parameter.WhileLoopDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;

@Service
@Slf4j
public class AddWhileLoopCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    private final ActivityService activityService;

    @Autowired
    public AddWhileLoopCallExecutor(ToolCallArgumentsParser callArgumentsParser, SessionStateStore sessionStateStore, ActivityService activityService) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
        this.activityService = activityService;
    }

    @Override
    public String getFunctionName() {
        return AddWhileLoopFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, List<String>> executeCall(String callArgumentsJson) {
        Result<WhileLoopDto, List<String>> argumentsParsingResult = callArgumentsParser.parseArguments(callArgumentsJson, WhileLoopDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        WhileLoopDto callArguments = argumentsParsingResult.getValue();

        BpmnModel model = sessionStateStore.model();
        String checkTaskName = callArguments.checkTask();
        Optional<String> optionalCheckTaskElementId = model.findElementByModelFriendlyId(checkTaskName);
        String checkTaskId;
        Set<String> predecessorTaskSuccessorsBeforeModification;
        Set<String> addedActivitiesNames = new HashSet<>();
        if (optionalCheckTaskElementId.isPresent()) {
            checkTaskId = optionalCheckTaskElementId.get();
            predecessorTaskSuccessorsBeforeModification = model.findSuccessors(checkTaskId);
        } else {
            Optional<String> optionalPredecessorElementId = model.findElementByModelFriendlyId(callArguments.predecessorElement());
            if (optionalPredecessorElementId.isEmpty()) {
                log.warn("Call unsuccessful, predecessor element does not exist in the model");
                return Result.error(List.of("Predecessor element does not exist in the model"));
            }

            String predecessorElementId = optionalPredecessorElementId.get();
            predecessorTaskSuccessorsBeforeModification = model.findSuccessors(predecessorElementId);
            checkTaskId = model.addTask(checkTaskName, checkTaskName);
            addedActivitiesNames.add(checkTaskName);
            model.addUnlabelledSequenceFlow(predecessorElementId, checkTaskId);
        }

        model.clearSuccessors(checkTaskId);

        String openingGatewayId = model.addGateway(EXCLUSIVE, callArguments.elementName() + " gateway");
        model.addUnlabelledSequenceFlow(checkTaskId, openingGatewayId);
        if (!predecessorTaskSuccessorsBeforeModification.isEmpty()) {
            if (predecessorTaskSuccessorsBeforeModification.size() > 1) {
                log.warn("Predecessor element has more than on successor, choosing the first one");
            }
            String nextTaskId = predecessorTaskSuccessorsBeforeModification.iterator().next();
            model.addLabelledSequenceFlow(openingGatewayId, nextTaskId, "false");
        }

        String previousElementInLoopId = openingGatewayId;
        for (Activity activityInLoop : callArguments.activitiesInLoop()) {
            Result<ActivityIdAndName, String> activityAddResult = activityService.addActivityToModel(model, activityInLoop);
            if (activityAddResult.isError()) {
                return Result.error(List.of(activityAddResult.getError()));
            }

            String activityId = activityAddResult.getValue().id();
            addedActivitiesNames.add(activityAddResult.getValue().modelFacingName());

            if (!model.areElementsDirectlyConnected(previousElementInLoopId, activityId)) {
                model.addUnlabelledSequenceFlow(previousElementInLoopId, activityId);
            }

            previousElementInLoopId = activityId;
        }

        if (!model.areElementsDirectlyConnected(previousElementInLoopId, checkTaskId)) {
            model.addUnlabelledSequenceFlow(previousElementInLoopId, checkTaskId);
        }

        return Result.ok("Added activities: " + addedActivitiesNames);
    }
}
