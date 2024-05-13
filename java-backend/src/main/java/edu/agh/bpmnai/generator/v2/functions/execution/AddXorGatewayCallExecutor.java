package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.NodeIdToModelInterfaceIdFunction;
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
import static edu.agh.bpmnai.generator.bpmn.model.HumanReadableId.isHumanReadableIdentifier;

@Service
@Slf4j
public class AddXorGatewayCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    private final InsertElementIntoDiagram insertElementIntoDiagram;

    private final NodeIdToModelInterfaceIdFunction nodeIdToModelInterfaceIdFunction;

    @Autowired
    public AddXorGatewayCallExecutor(
            ToolCallArgumentsParser callArgumentsParser,
            SessionStateStore sessionStateStore,
            InsertElementIntoDiagram insertElementIntoDiagram,
            NodeIdToModelInterfaceIdFunction nodeIdToModelInterfaceIdFunction
    ) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
        this.insertElementIntoDiagram = insertElementIntoDiagram;
        this.nodeIdToModelInterfaceIdFunction = nodeIdToModelInterfaceIdFunction;
    }

    @Override
    public String getFunctionName() {
        return AddXorGatewayFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson) {
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

        BpmnModel model = sessionStateStore.model();
        String checkTaskId;
        boolean checkTaskExistsInTheModel;
        Set<String> addedNodesIds = new HashSet<>();
        if (isHumanReadableIdentifier(callArguments.checkTask())) {
            String checkTaskModelInterfaceId = HumanReadableId.fromString(callArguments.checkTask()).id();
            Optional<String> checkTaskIdOptional = sessionStateStore.getElementId(checkTaskModelInterfaceId);
            if (checkTaskIdOptional.isEmpty()) {
                return Result.error("Check task '%s' does not exist in the model".formatted(callArguments.checkTask()));
            }

            checkTaskId = checkTaskIdOptional.get();
            checkTaskExistsInTheModel = true;
        } else {
            checkTaskId = model.addTask(callArguments.checkTask());
            addedNodesIds.add(checkTaskId);
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
                return Result.error(
                        ("Predecessor element '%s' does not exist in the model. Provide an element which exists in the"
                         + " model.").formatted(
                                callArguments.predecessorElement()));
            }

            subdiagramPredecessorElement = callArguments.predecessorElement().id();
            subdiagramStartElement = checkTaskId;
        }

        if (model.findSuccessors(subdiagramPredecessorElement).size() > 1) {
            return Result.error(
                    ("Element '%s' which was designated as a predecessor element has more than one successor. Provide "
                     + "an element with exactly 0 or 1 successors to avoid ambiguity.").formatted(
                            model.getHumanReadableId(subdiagramStartElement).orElseThrow()));
        }

        String openingGatewayId = model.addGateway(EXCLUSIVE, callArguments.elementName() + " opening gateway");
        addedNodesIds.add(openingGatewayId);
        if (subdiagramStartElement == null) {
            subdiagramStartElement = openingGatewayId;
        }

        String closingGatewayId = model.addGateway(EXCLUSIVE, callArguments.elementName() + " closing gateway");
        addedNodesIds.add(closingGatewayId);

        model.addUnlabelledSequenceFlow(checkTaskId, openingGatewayId);

        for (Activity activityInGateway : callArguments.activitiesInsideGateway()) {
            if (model.findElementByName(activityInGateway.activityName()).isPresent()) {
                return Result.error("Element with name %s already exists in the model".formatted(activityInGateway.activityName()));
            }

            String taskId = model.addTask(activityInGateway.activityName());
            addedNodesIds.add(taskId);
            model.addUnlabelledSequenceFlow(openingGatewayId, taskId);
            if (activityInGateway.isProcessEnd()) {
                String endEventId = model.addEndEvent();
                addedNodesIds.add(endEventId);
                model.addUnlabelledSequenceFlow(taskId, endEventId);
            } else {
                model.addUnlabelledSequenceFlow(taskId, closingGatewayId);
            }
        }

        String subdiagramClosingElement = closingGatewayId;
        Set<String> closingGatewayPredecessors = model.findPredecessors(closingGatewayId);
        if (closingGatewayPredecessors.size() == 1) {
            // A gateway with a single predecessor is useless, so just remove it
            model.removeFlowNode(closingGatewayId);
            addedNodesIds.remove(closingGatewayId);
            subdiagramClosingElement = closingGatewayPredecessors.iterator().next();
        }

        Result<Void, String> elementInsertResult = insertElementIntoDiagram.apply(
                subdiagramPredecessorElement,
                subdiagramStartElement,
                subdiagramClosingElement,
                model
        );

        if (elementInsertResult.isError()) {
            return Result.error(elementInsertResult.getError());
        }

        sessionStateStore.setModel(model);
        for (String nodeId : addedNodesIds) {
            sessionStateStore.setModelInterfaceId(nodeId, nodeIdToModelInterfaceIdFunction.apply(nodeId));
        }

        return Result.ok("Call successful");
    }
}
