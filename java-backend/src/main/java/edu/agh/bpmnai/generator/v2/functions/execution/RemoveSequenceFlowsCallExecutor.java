package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.bpmn.model.RemoveSequenceFlowError;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.RemoveSequenceFlowsFunction;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveSequenceFlowsCallParameterDto;
import edu.agh.bpmnai.generator.v2.functions.parameter.SequenceFlowDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static edu.agh.bpmnai.generator.bpmn.model.HumanReadableId.isHumanReadableIdentifier;

@Service
@Slf4j
public class RemoveSequenceFlowsCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    @Autowired
    public RemoveSequenceFlowsCallExecutor(
            ToolCallArgumentsParser callArgumentsParser, SessionStateStore sessionStateStore
    ) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
    }

    @Override
    public String getFunctionName() {
        return RemoveSequenceFlowsFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson) {
        Result<RemoveSequenceFlowsCallParameterDto, String> argumentsParsingResult =
                callArgumentsParser.parseArguments(callArgumentsJson, RemoveSequenceFlowsCallParameterDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        BpmnModel model = sessionStateStore.model();
        List<SequenceFlowDto> sequenceFlowDtos =
                argumentsParsingResult.getValue().sequenceFlowsToRemove();
        StringBuilder removedFlowsMessageBuilder = new StringBuilder("Following sequence flows were removed:\n");
        StringBuilder missingFlowsMessageBuilder = new StringBuilder(
                "Following sequence flows are not present in the diagram:\n");
        for (SequenceFlowDto sequenceFlowDto : sequenceFlowDtos) {
            if (!isHumanReadableIdentifier(sequenceFlowDto.source())) {
                return Result.error("'%s' is not in the correct format".formatted(sequenceFlowDto.source()));
            }

            String sequenceFlowSourceModelFacingId = HumanReadableId.fromString(sequenceFlowDto.source()).id();
            Optional<String> sequenceFlowSourceId = sessionStateStore.getNodeId(sequenceFlowSourceModelFacingId);
            if (sequenceFlowSourceId.isEmpty()) {
                return Result.error("Element with id '%s' does not exist in the diagram".formatted(
                        sequenceFlowDto.target()));
            }

            if (!isHumanReadableIdentifier(sequenceFlowDto.source())) {
                return Result.error("'%s' is not in the correct format".formatted(sequenceFlowDto.source()));
            }

            String sequenceFlowTargetModelFacingId = HumanReadableId.fromString(sequenceFlowDto.target()).id();
            Optional<String> sequenceFlowTargetId = sessionStateStore.getNodeId(sequenceFlowTargetModelFacingId);
            if (sequenceFlowTargetId.isEmpty()) {
                return Result.error("Element with id '%s' does not exist in the diagram".formatted(
                        sequenceFlowDto.target()));
            }

            Result<Void, RemoveSequenceFlowError> removeResult = model.removeSequenceFlow(
                    sequenceFlowSourceId.get(), sequenceFlowTargetId.get());

            if (removeResult.isError()) {
                switch (removeResult.getError()) {
                    case SOURCE_ELEMENT_NOT_FOUND -> {
                        return Result.error("Could not find element with id '%s'".formatted(
                                sequenceFlowDto.source()));
                    }
                    case TARGET_ELEMENT_NOT_FOUND -> {
                        return Result.error("Could not find element with id '%s'".formatted(
                                sequenceFlowDto.target()));
                    }
                    case SOURCE_ELEMENT_NOT_FLOW_NODE -> {
                        return Result.error("Element '%s' is not a valid sequence flow source".formatted(
                                sequenceFlowDto.source()));
                    }
                    case TARGET_ELEMENT_NOT_FLOW_NODE -> {
                        return Result.error("Element '%s' is not a valid sequence flow target".formatted(
                                sequenceFlowDto.target()));
                    }
                    case ELEMENTS_NOT_CONNECTED -> missingFlowsMessageBuilder.append(sequenceFlowDto.source())
                            .append(" -> ")
                            .append(
                                    sequenceFlowDto.target())
                            .append(", ");
                }
            } else {
                removedFlowsMessageBuilder.append(sequenceFlowDto.source()).append(" -> ").append(
                        sequenceFlowDto.target()).append(", ");
            }
        }

        sessionStateStore.setModel(model);

        return Result.ok(removedFlowsMessageBuilder.append('\n').append(missingFlowsMessageBuilder).toString());
    }
}
