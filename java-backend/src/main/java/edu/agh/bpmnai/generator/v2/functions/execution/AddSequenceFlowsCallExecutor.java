package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.AddSequenceFlowsFunction;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.AddSequenceFlowsCallParameterDto;
import edu.agh.bpmnai.generator.v2.functions.parameter.SequenceFlowDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static edu.agh.bpmnai.generator.bpmn.model.HumanReadableId.isHumanReadableIdentifier;

@Service
@Slf4j
public class AddSequenceFlowsCallExecutor implements FunctionCallExecutor {

    private final SessionStateStore sessionStateStore;
    private final ToolCallArgumentsParser callArgumentsParser;

    @Autowired
    public AddSequenceFlowsCallExecutor(
            SessionStateStore sessionStateStore,
            ToolCallArgumentsParser callArgumentsParser
    ) {
        this.sessionStateStore = sessionStateStore;
        this.callArgumentsParser = callArgumentsParser;
    }

    @Override
    public String getFunctionName() {
        return AddSequenceFlowsFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson) {
        Result<AddSequenceFlowsCallParameterDto, String> argumentsParsingResult = callArgumentsParser.parseArguments(
                callArgumentsJson, AddSequenceFlowsCallParameterDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        AddSequenceFlowsCallParameterDto callArguments = argumentsParsingResult.getValue();

        BpmnModel model = sessionStateStore.model();
        for (SequenceFlowDto sequenceFlow : callArguments.sequenceFlows()) {
            if (!isHumanReadableIdentifier(sequenceFlow.source())) {
                return Result.error("'%s' is not in the correct id format".formatted(sequenceFlow.source()));
            }

            if (!isHumanReadableIdentifier(sequenceFlow.target())) {
                return Result.error("'%s' is not in the correct id format".formatted(sequenceFlow.target()));
            }

            String sourceNodeLlmInterfacingId = HumanReadableId.fromString(sequenceFlow.source()).id();
            Optional<String> sourceNodeModelId = sessionStateStore.getNodeId(sourceNodeLlmInterfacingId);
            if (sourceNodeModelId.isEmpty()) {
                return Result.error("Node with id '%s' does not exist".formatted(sequenceFlow.source()));
            }

            String targetNodeLlmInterfacingId = HumanReadableId.fromString(sequenceFlow.target()).id();
            Optional<String> targetNodeModelId = sessionStateStore.getNodeId(targetNodeLlmInterfacingId);
            if (targetNodeModelId.isEmpty()) {
                return Result.error("Node with id '%s' does not exist".formatted(sequenceFlow.target()));
            }

            if (model.areElementsDirectlyConnected(sourceNodeModelId.get(), targetNodeModelId.get())) {
                return Result.error("Elements '%s' and '%s' are already connected via a sequence flow".formatted(
                        sequenceFlow.source(),
                        sequenceFlow.target()
                ));
            }

            model.addUnlabelledSequenceFlow(sourceNodeModelId.get(), targetNodeModelId.get());
        }

        sessionStateStore.setModel(model);

        return Result.ok("Call successful");
    }
}
