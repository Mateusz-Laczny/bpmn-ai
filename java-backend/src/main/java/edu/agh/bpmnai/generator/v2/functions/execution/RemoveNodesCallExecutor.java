package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.bpmn.model.RemoveActivityError;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import edu.agh.bpmnai.generator.v2.functions.RemoveNodesFunction;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveNodesFunctionCallDto;
import edu.agh.bpmnai.generator.v2.session.ImmutableSessionState;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.agh.bpmnai.generator.bpmn.model.HumanReadableId.isHumanReadableIdentifier;

@Service
@Slf4j
public class RemoveNodesCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    @Autowired
    public RemoveNodesCallExecutor(
            ToolCallArgumentsParser callArgumentsParser,
            SessionStateStore sessionStateStore
    ) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
    }

    @Override
    public String getFunctionName() {
        return RemoveNodesFunction.FUNCTION_NAME;
    }

    @Override
    public Result<FunctionCallResult, String> executeCall(
            String callArgumentsJson,
            ImmutableSessionState sessionState
    ) {
        Result<RemoveNodesFunctionCallDto, String> argumentsParsingResult = callArgumentsParser.parseArguments(
                callArgumentsJson, RemoveNodesFunctionCallDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        RemoveNodesFunctionCallDto callArguments = argumentsParsingResult.getValue();

        BpmnModel model = sessionState.bpmnModel();
        Set<String> removedNodesIds = new HashSet<>();
        StringBuilder removedElementsMessageBuilder = new StringBuilder("Following elements were removed:\n");
        StringBuilder missingElementsMessageBuilder = new StringBuilder(
                "Following elements are not present in the diagram:\n");
        for (String nodeToRemove : callArguments.nodesToRemove()) {
            if (!isHumanReadableIdentifier(nodeToRemove)) {
                return Result.error("'%s' is not in the correct format".formatted(nodeToRemove));
            }

            String nodeToRemoveModelFacingId = HumanReadableId.fromString(nodeToRemove).id();
            Optional<String> nodeToRemoveIdOptional = sessionState.getNodeId(nodeToRemoveModelFacingId);
            if (nodeToRemoveIdOptional.isEmpty()) {
                missingElementsMessageBuilder.append(nodeToRemove).append(", ");
            } else {
                String nodeToRemoveId = nodeToRemoveIdOptional.get();
                Result<Void, RemoveActivityError> removeFlowNodeResult =
                        model.removeFlowNode(nodeToRemoveId);
                if (removeFlowNodeResult.isOk()) {
                    removedElementsMessageBuilder.append(nodeToRemove).append(", ");
                    removedNodesIds.add(nodeToRemoveId);
                } else {
                    log.warn(
                            "Unexpected error '{}' when removing element with model ID '{}'",
                            removeFlowNodeResult.getError(),
                            nodeToRemove
                    );
                }
            }
        }

        Map<String, String> updatedNodeIdToModelInterfaceId =
                sessionState.nodeIdToModelInterfaceId().entrySet().stream().filter(entry -> !removedNodesIds.contains(
                        entry.getKey())).collect(
                        Collectors.toMap(Entry::getKey, Entry::getValue));
        ImmutableSessionState updatedState = ImmutableSessionState.builder().from(sessionState)
                .bpmnModel(model)
                .nodeIdToModelInterfaceId(updatedNodeIdToModelInterfaceId)
                .build();

        return Result.ok(new FunctionCallResult(
                updatedState,
                removedElementsMessageBuilder.append('\n')
                        .append(missingElementsMessageBuilder)
                        .toString()
        ));
    }
}
