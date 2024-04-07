package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.AddXorGatewayFunction;
import edu.agh.bpmnai.generator.v2.functions.InsertElementIntoDiagram;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
import edu.agh.bpmnai.generator.v2.functions.parameter.XorGatewayDto;
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
public class AddXorGatewayCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    private final InsertElementIntoDiagram insertElementIntoDiagram;

    @Autowired
    public AddXorGatewayCallExecutor(
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
        return AddXorGatewayFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson, BpmnManagedReference modelReference) {
        Result<XorGatewayDto, String> argumentsParsingResult = callArgumentsParser.parseArguments(
                callArgumentsJson,
                XorGatewayDto.class
        );
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        XorGatewayDto callArguments = argumentsParsingResult.getValue();

        if (callArguments.activitiesInsideGateway().size() < 2) {
            return Result.error("A gateway must contain at least 2 activities");
        }

        BpmnModel model = modelReference.getCurrentValue();
        String checkTaskName = callArguments.checkActivity();
        Optional<String> optionalTaskElementId = model.findElementByModelFriendlyId(checkTaskName);
        String checkTaskId;
        String subdiagramPredecessorElement;
        String subdiagramStartElement = null;
        Set<String> addedActivitiesNames = new HashSet<>();
        if (optionalTaskElementId.isPresent()) {
            checkTaskId = optionalTaskElementId.get();
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
            addedActivitiesNames.add(checkTaskName);
            subdiagramStartElement = checkTaskId;
        }

        String openingGatewayId = model.addGateway(EXCLUSIVE, callArguments.elementName() + " opening gateway");
        if (subdiagramStartElement == null) {
            subdiagramStartElement = openingGatewayId;
        }

        String closingGatewayId = model.addGateway(EXCLUSIVE, callArguments.elementName() + " closing gateway");

        model.addUnlabelledSequenceFlow(checkTaskId, openingGatewayId);

        for (Activity activityInGateway : callArguments.activitiesInsideGateway()) {
            if (model.findElementByModelFriendlyId(activityInGateway.activityName()).isPresent()) {
                return Result.error("Element with name %s already exists in the model".formatted(activityInGateway.activityName()));
            }

            String activityId = model.addTask(activityInGateway.activityName(), activityInGateway.activityName());
            addedActivitiesNames.add(activityInGateway.activityName());
            model.addUnlabelledSequenceFlow(openingGatewayId, activityId);
            if (activityInGateway.isProcessEnd()) {
                String endEventId = model.addEndEvent();
                model.addUnlabelledSequenceFlow(activityId, endEventId);
            } else {
                model.addUnlabelledSequenceFlow(activityId, closingGatewayId);
            }
        }

        Result<Void, String> elementInsertResult = insertElementIntoDiagram.apply(
                subdiagramPredecessorElement,
                subdiagramStartElement,
                closingGatewayId,
                model
        );

        if (elementInsertResult.isError()) {
            return Result.error(elementInsertResult.getError());
        }

        modelReference.setValue(model);

        return Result.ok("Added activities: " + addedActivitiesNames);
    }
}
