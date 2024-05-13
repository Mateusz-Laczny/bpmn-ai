package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.NodeIdToModelInterfaceIdFunction;
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
import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.PARALLEL;

@Service
@Slf4j
public class AddParallelGatewayCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    private final InsertElementIntoDiagram insertElementIntoDiagram;

    private final NodeIdToModelInterfaceIdFunction nodeIdToModelInterfaceIdFunction;

    @Autowired
    public AddParallelGatewayCallExecutor(
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
        return AddParallelGatewayFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson) {
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

        BpmnModel model = sessionStateStore.model();
        Set<String> addedNodesIds = new HashSet<>();

        Optional<String> predecessorNodeIdOptional =
                sessionStateStore.getElementId(callArguments.predecessorElement().id());
        if (predecessorNodeIdOptional.isEmpty()) {
            return Result.error("Predecessor element '%s' does not exist in the model".formatted(callArguments.predecessorElement()
                                                                                                         .asString()));
        }

        String predecessorElementId = predecessorNodeIdOptional.get();

        if (model.findSuccessors(predecessorElementId).size() > 1) {
            return Result.error(
                    ("Predecessor element '%s' has more than one successor; inserting an element after it would be "
                     + "ambiguous. Provide a predecessor element with a single or no successors").formatted(
                            callArguments.predecessorElement()));
        }

        String openingGatewayId = model.addGateway(PARALLEL, callArguments.elementName() + " opening gateway");
        addedNodesIds.add(openingGatewayId);
        String closingGatewayId = model.addGateway(PARALLEL, callArguments.elementName() + " closing gateway");
        addedNodesIds.add(closingGatewayId);

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

        Result<Void, String> insertSubdiagramResult = insertElementIntoDiagram.apply(
                predecessorElementId,
                openingGatewayId,
                subdiagramClosingElement,
                model
        );

        if (insertSubdiagramResult.isError()) {
            return Result.error(insertSubdiagramResult.getError());
        }

        sessionStateStore.setModel(model);
        for (String nodeId : addedNodesIds) {
            sessionStateStore.setModelInterfaceId(nodeId, nodeIdToModelInterfaceIdFunction.apply(nodeId));
        }

        return Result.ok("Call successful");
    }
}
