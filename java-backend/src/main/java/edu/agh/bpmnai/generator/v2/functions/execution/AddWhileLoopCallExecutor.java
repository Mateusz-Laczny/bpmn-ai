package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
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
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;
import static edu.agh.bpmnai.generator.bpmn.model.HumanReadableId.isHumanReadableIdentifier;

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
        String checkTaskId;
        boolean checkTaskExistsInTheModel;
        if (isHumanReadableIdentifier(callArguments.checkTask())) {
            checkTaskId = HumanReadableId.fromString(callArguments.checkTask()).id();
            if (!model.doesIdExist(checkTaskId)) {
                return Result.error("Check task '%s' does not exist in the model".formatted(callArguments.checkTask()));
            }
            checkTaskExistsInTheModel = true;
        } else {
            checkTaskId = model.addTask(callArguments.checkTask());
            checkTaskExistsInTheModel = false;
        }
        String subdiagramPredecessorElement;
        String subdiagramStartElement = null;
        Set<String> addedActivitiesNames = new HashSet<>();
        if (checkTaskExistsInTheModel) {
            subdiagramPredecessorElement = checkTaskId;
        } else {
            if (!model.doesIdExist(callArguments.predecessorElement().id())) {
                log.warn(
                        "Call unsuccessful, predecessor element '{}' does not exist in the model",
                        callArguments.predecessorElement()
                );
                return Result.error("Predecessor element %s does not exist in the model".formatted(callArguments.predecessorElement()));
            }

            subdiagramPredecessorElement = callArguments.predecessorElement().id();
            subdiagramStartElement = checkTaskId;
            addedActivitiesNames.add(callArguments.checkTask());
        }

        String gatewayId = model.addGateway(EXCLUSIVE, callArguments.elementName() + " gateway");
        if (subdiagramStartElement == null) {
            subdiagramStartElement = gatewayId;
        } else {
            model.addUnlabelledSequenceFlow(checkTaskId, gatewayId);
        }

        String previousElementInLoopId = gatewayId;
        for (Activity activityInLoop : callArguments.activitiesInLoop()) {
            if (model.findElementByName(activityInLoop.activityName()).isPresent()) {
                return Result.error("Element %s already exists in the model".formatted(activityInLoop.activityName()));
            }

            String activityId = model.addTask(activityInLoop.activityName());
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
