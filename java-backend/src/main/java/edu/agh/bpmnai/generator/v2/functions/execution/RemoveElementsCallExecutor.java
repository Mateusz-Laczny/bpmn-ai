package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.bpmn.model.RemoveActivityError;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.RemoveElementsFunction;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveElementsFunctionCallDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class RemoveElementsCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    @Autowired
    public RemoveElementsCallExecutor(
            ToolCallArgumentsParser callArgumentsParser,
            SessionStateStore sessionStateStore
    ) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
    }

    @Override
    public String getFunctionName() {
        return RemoveElementsFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson) {
        Result<RemoveElementsFunctionCallDto, String> argumentsParsingResult = callArgumentsParser.parseArguments(
                callArgumentsJson, RemoveElementsFunctionCallDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        RemoveElementsFunctionCallDto callArguments = argumentsParsingResult.getValue();

        BpmnModel model = sessionStateStore.model();
        Set<String> removedNodesIds = new HashSet<>();
        StringBuilder removedElementsMessageBuilder = new StringBuilder("Following elements were removed:\n");
        StringBuilder missingElementsMessageBuilder = new StringBuilder(
                "Following elements are not present in the diagram:\n");
        for (HumanReadableId elementToRemove : callArguments.elementsToRemove()) {
            String nodeToRemoveModelFacingId = elementToRemove.id();
            Optional<String> nodeToRemoveIdOptional = sessionStateStore.getElementId(nodeToRemoveModelFacingId);
            if (nodeToRemoveIdOptional.isEmpty()) {
                missingElementsMessageBuilder.append(elementToRemove.asString()).append(", ");
            } else {
                String nodeToRemoveId = nodeToRemoveIdOptional.get();
                Result<Void, RemoveActivityError> removeFlowNodeResult =
                        model.removeFlowNode(nodeToRemoveId);
                if (removeFlowNodeResult.isOk()) {
                    removedElementsMessageBuilder.append(elementToRemove).append(", ");
                    removedNodesIds.add(nodeToRemoveId);
                } else {
                    log.warn(
                            "Unexpected error '{}' when removing element with model ID '{}'",
                            removeFlowNodeResult.getError(),
                            elementToRemove
                    );
                }
            }
        }

        sessionStateStore.setModel(model);
        for (String nodeId : removedNodesIds) {
            sessionStateStore.removeModelInterfaceId(nodeId);
        }

        return Result.ok(removedElementsMessageBuilder.append('\n').append(missingElementsMessageBuilder).toString());
    }
}
