package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.NodeIdToModelInterfaceIdFunction;
import edu.agh.bpmnai.generator.v2.functions.AddParallelGatewayFunction;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import edu.agh.bpmnai.generator.v2.functions.InsertElementIntoDiagram;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.ParallelGatewayDto;
import edu.agh.bpmnai.generator.v2.functions.parameter.Task;
import edu.agh.bpmnai.generator.v2.session.ImmutableSessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.PARALLEL;
import static edu.agh.bpmnai.generator.bpmn.model.HumanReadableId.isHumanReadableIdentifier;

@Service
@Slf4j
public class AddParallelGatewayCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final InsertElementIntoDiagram insertElementIntoDiagram;

    private final NodeIdToModelInterfaceIdFunction nodeIdToModelInterfaceIdFunction;

    @Autowired
    public AddParallelGatewayCallExecutor(
            ToolCallArgumentsParser callArgumentsParser,
            InsertElementIntoDiagram insertElementIntoDiagram,
            NodeIdToModelInterfaceIdFunction nodeIdToModelInterfaceIdFunction
    ) {
        this.callArgumentsParser = callArgumentsParser;
        this.insertElementIntoDiagram = insertElementIntoDiagram;
        this.nodeIdToModelInterfaceIdFunction = nodeIdToModelInterfaceIdFunction;
    }

    @Override
    public String getFunctionName() {
        return AddParallelGatewayFunction.FUNCTION_NAME;
    }

    @Override
    public Result<FunctionCallResult, String> executeCall(
            String callArgumentsJson, ImmutableSessionState sessionState
    ) {
        Result<ParallelGatewayDto, String> argumentsParsingResult =
                callArgumentsParser.parseArguments(callArgumentsJson, ParallelGatewayDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        ParallelGatewayDto callArguments = argumentsParsingResult.getValue();

        if (callArguments.tasksInsideGateway().size() < 2) {
            return Result.error("A gateway subprocess must contain at least 2 activities");
        }

        BpmnModel model = sessionState.bpmnModel();
        Set<String> addedNodesIds = new HashSet<>();

        if (!isHumanReadableIdentifier(callArguments.insertionPoint())) {
            return Result.error("'%s' is not in the correct format".formatted(callArguments.insertionPoint()));
        }

        HumanReadableId insertionPointModelFacingId = HumanReadableId.fromString(callArguments.insertionPoint());
        Optional<String> insertionPointModelId = sessionState.getNodeId(insertionPointModelFacingId.id());
        if (insertionPointModelId.isEmpty()) {
            return Result.error("Insertion point '%s' doesn't exist in the diagram".formatted(callArguments.insertionPoint()));
        }

        String insertionPointId = insertionPointModelId.get();

        if (model.findSuccessors(insertionPointId).size() > 1) {
            return Result.error(("Insertion point '%s' has more than one successor node; inserting a subprocess after"
                                 + " it would be "
                                 + "ambiguous. Provide an insertion point with exactly 0 or 1 successor nodes").formatted(
                    callArguments.insertionPoint()));
        }

        String openingGatewayId = model.addGateway(PARALLEL, callArguments.subprocessName() + " opening gateway");
        addedNodesIds.add(openingGatewayId);
        String closingGatewayId = model.addGateway(PARALLEL, callArguments.subprocessName() + " closing gateway");
        addedNodesIds.add(closingGatewayId);

        for (Task taskInGateway : callArguments.tasksInsideGateway()) {
            if (model.findElementByName(taskInGateway.taskName()).isPresent()) {
                return Result.error("Node with name '%s' already exists in the diagram".formatted(taskInGateway.taskName()));
            }

            String taskId = model.addTask(taskInGateway.taskName());
            addedNodesIds.add(taskId);
            model.addUnlabelledSequenceFlow(openingGatewayId, taskId);
            if (taskInGateway.isProcessEnd()) {
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
                insertionPointId,
                openingGatewayId,
                subdiagramClosingElement,
                model
        );

        if (insertSubdiagramResult.isError()) {
            return Result.error(insertSubdiagramResult.getError());
        }

        var updatedState = sessionState.withModel(model);
        updatedState = updatedState.withNodeIdToModelInterfaceId(nodeIdToModelInterfaceIdFunction.apply(
                addedNodesIds,
                updatedState
        ));
        HumanReadableId subprocessStartNode = new HumanReadableId(
                model.getName(openingGatewayId).orElseThrow(),
                updatedState.getModelInterfaceId(openingGatewayId)
                        .orElseThrow()
        );
        HumanReadableId subprocessEndNode = new HumanReadableId(
                model.getName(subdiagramClosingElement).orElseThrow(),
                updatedState.getModelInterfaceId(
                        subdiagramClosingElement).orElseThrow()
        );

        return Result.ok(new FunctionCallResult(
                updatedState,
                ("Call successful; subprocess start node: '%s', subprocess end node: "
                 + "'%s'").formatted(
                        subprocessStartNode,
                        subprocessEndNode
                )
        ));
    }
}
