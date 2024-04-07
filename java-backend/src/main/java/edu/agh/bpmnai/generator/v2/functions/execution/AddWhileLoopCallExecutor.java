package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.AddWhileLoopFunction;
import edu.agh.bpmnai.generator.v2.functions.InsertElementIntoDiagram;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
import edu.agh.bpmnai.generator.v2.functions.parameter.WhileLoopDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;

@Service
@Slf4j
public class AddWhileLoopCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    private final ActivityService activityService;

    private final InsertElementIntoDiagram insertElementIntoDiagram;

    @Autowired
    public AddWhileLoopCallExecutor(
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
        String checkTaskName = callArguments.checkTask();
        Optional<String> optionalCheckTaskElementId = model.findElementByModelFriendlyId(checkTaskName);
        String checkTaskId;
        String subdiagramPredecessorElement;
        String subdiagramStartElement = null;
        Set<String> addedActivitiesNames = new HashSet<>();
        if (optionalCheckTaskElementId.isPresent()) {
            checkTaskId = optionalCheckTaskElementId.get();
            subdiagramPredecessorElement = checkTaskId;
        } else {
            Optional<String> optionalPredecessorElementId =
                    model.findElementByModelFriendlyId(callArguments.predecessorElement());
            if (optionalPredecessorElementId.isEmpty()) {
                log.warn("Call unsuccessful, predecessor element does not exist in the model");
                return Result.error("Predecessor element does not exist in the model");
            }

            subdiagramPredecessorElement = optionalPredecessorElementId.get();
            checkTaskId = model.addTask(checkTaskName, checkTaskName);
            subdiagramStartElement = checkTaskId;
            addedActivitiesNames.add(checkTaskName);
        }

        String gatewayId = model.addGateway(EXCLUSIVE, callArguments.elementName() + " gateway");
        if (subdiagramStartElement == null) {
            subdiagramStartElement = gatewayId;
        } else {
            model.addUnlabelledSequenceFlow(checkTaskId, gatewayId);
        }

        String previousElementInLoopId = gatewayId;
        for (Activity activityInLoop : callArguments.activitiesInLoop()) {
            Result<ActivityIdAndName, String> activityAddResult = activityService.addActivityToModel(
                    model,
                    activityInLoop
            );
            if (activityAddResult.isError()) {
                return Result.error(activityAddResult.getError());
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

        Result<Void, String> insertSubdiagramResult = insertElementIntoDiagram.apply(
                subdiagramPredecessorElement,
                subdiagramStartElement,
                gatewayId,
                model
        );

        if (insertSubdiagramResult.isError()) {
            return Result.error(insertSubdiagramResult.getError());
        }

        return Result.ok("Added activities: " + addedActivitiesNames);
    }
}
