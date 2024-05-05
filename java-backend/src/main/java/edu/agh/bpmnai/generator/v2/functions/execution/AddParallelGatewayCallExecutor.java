package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.AddParallelGatewayFunction;
import edu.agh.bpmnai.generator.v2.functions.InsertElementIntoDiagram;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
import edu.agh.bpmnai.generator.v2.functions.parameter.ParallelGatewayDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.PARALLEL;

@Service
@Slf4j
public class AddParallelGatewayCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    private final InsertElementIntoDiagram insertElementIntoDiagram;

    @Autowired
    public AddParallelGatewayCallExecutor(
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
        return AddParallelGatewayFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson, BpmnManagedReference modelReference) {
        Result<ParallelGatewayDto, String> argumentsParsingResult = callArgumentsParser.parseArguments(
                callArgumentsJson,
                ParallelGatewayDto.class
        );
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        ParallelGatewayDto callArguments = argumentsParsingResult.getValue();

        if (callArguments.activitiesInsideGateway().size() < 2) {
            return Result.error("A gateway must contain at least 2 activities");
        }

        BpmnModel model = modelReference.getCurrentValue();

        String predecessorElementId = callArguments.predecessorElement().id();
        if (!model.doesIdExist(predecessorElementId)) {
            return Result.error("Predecessor element '%s' does not exist in the model".formatted(callArguments.predecessorElement()));
        }

        String openingGatewayId = model.addGateway(PARALLEL, callArguments.elementName() + " opening gateway");
        String closingGatewayId = model.addGateway(PARALLEL, callArguments.elementName() + " closing gateway");

        Set<String> addedActivitiesNames = new HashSet<>();
        for (Activity activityInGateway : callArguments.activitiesInsideGateway()) {
            if (model.findElementByName(activityInGateway.activityName()).isPresent()) {
                return Result.error("Element with name %s already exists in the model".formatted(activityInGateway.activityName()));
            }

            String activityId = model.addTask(activityInGateway.activityName());
            addedActivitiesNames.add(activityInGateway.activityName());
            model.addUnlabelledSequenceFlow(openingGatewayId, activityId);
            if (activityInGateway.isProcessEnd()) {
                String endEventId = model.addEndEvent();
                model.addUnlabelledSequenceFlow(activityId, endEventId);
            } else {
                model.addUnlabelledSequenceFlow(activityId, closingGatewayId);
            }
        }

        Result<Void, String> insertSubdiagramResult = insertElementIntoDiagram.apply(
                predecessorElementId,
                openingGatewayId,
                closingGatewayId,
                model
        );

        if (insertSubdiagramResult.isError()) {
            return Result.error(insertSubdiagramResult.getError());
        }

        modelReference.setValue(model);

        return Result.ok("Added activities: " + addedActivitiesNames);
    }
}
