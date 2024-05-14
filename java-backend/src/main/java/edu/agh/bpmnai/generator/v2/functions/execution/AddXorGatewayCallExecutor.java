package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.NodeIdToModelInterfaceIdFunction;
import edu.agh.bpmnai.generator.v2.functions.AddXorGatewayFunction;
import edu.agh.bpmnai.generator.v2.functions.InsertElementIntoDiagram;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.Task;
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

        if (callArguments.tasksInsideGateway().size() < 2) {
            return Result.error("A gateway must contain at least 2 activities");
        }

        BpmnModel model = sessionStateStore.model();
        String checkTaskId;
        boolean checkTaskExistsInTheModel;
        Set<String> addedNodesIds = new HashSet<>();
        if (isHumanReadableIdentifier(callArguments.checkTask())) {
            String checkTaskModelInterfaceId = HumanReadableId.fromString(callArguments.checkTask()).id();
            Optional<String> checkTaskIdOptional = sessionStateStore.getNodeId(checkTaskModelInterfaceId);
            if (checkTaskIdOptional.isEmpty()) {
                return Result.error("Check task '%s' does not exist in the diagram".formatted(callArguments.checkTask()));
            }

            checkTaskId = checkTaskIdOptional.get();
            checkTaskExistsInTheModel = true;
        } else {
            checkTaskId = model.addTask(callArguments.checkTask());
            addedNodesIds.add(checkTaskId);
            checkTaskExistsInTheModel = false;
        }

        String subdiagramInsertionPoint;
        String subdiagramStartElement = null;
        if (checkTaskExistsInTheModel) {
            subdiagramInsertionPoint = checkTaskId;
        } else {
            if (callArguments.insertionPoint() == null) {
                log.warn(
                        "Call unsuccessful, insertion point is null when check task '{}' does not exist in the "
                        + "diagram",
                        callArguments.checkTask()
                );
                return Result.error(("Call unsuccessful, insertion point is null even though check task '%s' does"
                                     + " not exist in"
                                     + " the diagram. Either use existing check task or provide an insertion point").formatted(
                        callArguments.checkTask()));
            }

            HumanReadableId insertionPointModelFacingId = HumanReadableId.fromString(callArguments.insertionPoint());
            Optional<String> insertionPointModelId = sessionStateStore.getNodeId(insertionPointModelFacingId.id());
            if (insertionPointModelId.isEmpty()) {
                log.warn(
                        "Call unsuccessful, insertion point '{}' does not exist in the diagram",
                        callArguments.insertionPoint()
                );
                return Result.error("Insertion point '%s' does not exist in the diagram".formatted(callArguments.insertionPoint()));
            }

            subdiagramInsertionPoint = insertionPointModelId.get();
            subdiagramStartElement = checkTaskId;
        }

        if (model.findSuccessors(subdiagramInsertionPoint).size() > 1) {
            return Result.error(
                    ("Insertion point '%s' has more than one successor node. Provide "
                     + "an insertion point with exactly 0 or 1 successor nodes to avoid ambiguity.").formatted(
                            model.getHumanReadableId(subdiagramStartElement).orElseThrow()));
        }

        String openingGatewayId = model.addGateway(EXCLUSIVE, callArguments.subprocessName() + " opening gateway");
        addedNodesIds.add(openingGatewayId);
        if (subdiagramStartElement == null) {
            subdiagramStartElement = openingGatewayId;
        }

        String closingGatewayId = model.addGateway(EXCLUSIVE, callArguments.subprocessName() + " closing gateway");
        addedNodesIds.add(closingGatewayId);

        model.addUnlabelledSequenceFlow(checkTaskId, openingGatewayId);

        for (Task taskInGateway : callArguments.tasksInsideGateway()) {
            if (model.findElementByName(taskInGateway.taskName()).isPresent()) {
                return Result.error("Node with name %s already exists".formatted(taskInGateway.taskName()));
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

        String subdiagramEndElement = closingGatewayId;
        Set<String> closingGatewayPredecessors = model.findPredecessors(closingGatewayId);
        if (closingGatewayPredecessors.size() == 1) {
            // A gateway with a single predecessor is useless, so just remove it
            model.removeFlowNode(closingGatewayId);
            addedNodesIds.remove(closingGatewayId);
            subdiagramEndElement = closingGatewayPredecessors.iterator().next();
        }

        Result<Void, String> elementInsertResult = insertElementIntoDiagram.apply(
                subdiagramInsertionPoint,
                subdiagramStartElement,
                subdiagramEndElement,
                model
        );

        if (elementInsertResult.isError()) {
            return Result.error(elementInsertResult.getError());
        }

        sessionStateStore.setModel(model);
        for (String nodeId : addedNodesIds) {
            sessionStateStore.setModelInterfaceId(nodeId, nodeIdToModelInterfaceIdFunction.apply(nodeId));
        }

        HumanReadableId subprocessStartNode = new HumanReadableId(
                model.getName(subdiagramStartElement).orElseThrow(),
                sessionStateStore.getModelInterfaceId(subdiagramStartElement).orElseThrow()
        );
        HumanReadableId subprocessEndNode = new HumanReadableId(
                model.getName(subdiagramEndElement).orElseThrow(),
                sessionStateStore.getModelInterfaceId(subdiagramEndElement).orElseThrow()
        );

        return Result.ok("Call successful; subprocess start node: '%s', subprocess end node: '%s'".formatted(
                subprocessStartNode,
                subprocessEndNode
        ));
    }
}
