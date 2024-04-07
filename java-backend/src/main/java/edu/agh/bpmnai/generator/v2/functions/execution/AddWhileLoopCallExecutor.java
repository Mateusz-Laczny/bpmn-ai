package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
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

    private final InsertElementIntoDiagram insertElementIntoDiagram;

    @Autowired
    public AddWhileLoopCallExecutor(
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
        return AddWhileLoopFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson, BpmnManagedReference modelReference) {
        Result<WhileLoopDto, String> argumentsParsingResult = callArgumentsParser.parseArguments(
                callArgumentsJson,
                WhileLoopDto.class
        );
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        WhileLoopDto callArguments = argumentsParsingResult.getValue();

        BpmnModel model = modelReference.getCurrentValue();
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
            if (model.findElementByModelFriendlyId(activityInLoop.activityName()).isPresent()) {
                return Result.error("Element %s already exists in the model".formatted(activityInLoop.activityName()));
            }

            String activityId = model.addTask(activityInLoop.activityName(), activityInLoop.activityName());
            addedActivitiesNames.add(activityInLoop.activityName());

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

        modelReference.setValue(model);

        return Result.ok("Added activities: " + addedActivitiesNames);
    }
}
