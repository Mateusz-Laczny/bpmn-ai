package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
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
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;
import static edu.agh.bpmnai.generator.bpmn.model.HumanReadableId.isHumanReadableIdentifier;

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
        String checkTaskId;
        boolean checkTaskExistsInTheModel;
        Set<String> addedActivitiesNames = new HashSet<>();
        if (isHumanReadableIdentifier(callArguments.checkTask())) {
            checkTaskId = HumanReadableId.fromString(callArguments.checkTask()).id();
            if (!model.doesIdExist(checkTaskId)) {
                return Result.error("Check task '%s' does not exist in the model".formatted(callArguments.checkTask()));
            }
            checkTaskExistsInTheModel = true;
        } else {
            checkTaskId = model.addTask(callArguments.checkTask());
            addedActivitiesNames.add(callArguments.checkTask());
            checkTaskExistsInTheModel = false;
        }

        String subdiagramPredecessorElement;
        String subdiagramStartElement = null;
        if (checkTaskExistsInTheModel) {
            subdiagramPredecessorElement = checkTaskId;
        } else {
            if (callArguments.predecessorElement() == null) {
                log.warn(
                        "Call unsuccessful, predecessor element is null when check task '{}' does not exist in the "
                        + "model",
                        callArguments.checkTask()
                );
                return Result.error(("Call unsuccessful, predecessor element is null even though check task '%s' does"
                                     + " not exist in"
                                     + " the model. Either use existing check task or provide a predecessor element "
                                     + "for new check"
                                     + " task").formatted(callArguments.checkTask()));
            }

            if (!model.doesIdExist(callArguments.predecessorElement().id())) {
                log.warn("Call unsuccessful, predecessor element does not exist in the model");
                return Result.error("Predecessor element does not exist in the model");
            }

            subdiagramPredecessorElement = callArguments.predecessorElement().id();
            subdiagramStartElement = checkTaskId;
        }

        String openingGatewayId = model.addGateway(EXCLUSIVE, callArguments.elementName() + " opening gateway");
        if (subdiagramStartElement == null) {
            subdiagramStartElement = openingGatewayId;
        }

        String closingGatewayId = model.addGateway(EXCLUSIVE, callArguments.elementName() + " closing gateway");

        model.addUnlabelledSequenceFlow(checkTaskId, openingGatewayId);

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
