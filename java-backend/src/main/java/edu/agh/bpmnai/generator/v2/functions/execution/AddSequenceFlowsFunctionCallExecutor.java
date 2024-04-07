package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
import edu.agh.bpmnai.generator.bpmn.model.AddSequenceFlowError;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.RemoveSequenceFlowsFunction;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.AddSequenceFlowsCallParameterDto;
import edu.agh.bpmnai.generator.v2.functions.parameter.SequenceFlowDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static edu.agh.bpmnai.generator.bpmn.model.AddSequenceFlowError.ELEMENTS_ALREADY_CONNECTED;

@Service
@Slf4j
public class AddSequenceFlowsFunctionCallExecutor implements FunctionCallExecutor {
    private final ToolCallArgumentsParser callArgumentsParser;
    private final SessionStateStore sessionStateStore;

    @Autowired
    public AddSequenceFlowsFunctionCallExecutor(
            ToolCallArgumentsParser callArgumentsParser,
            SessionStateStore sessionStateStore
    ) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
    }

    @Override
    public String getFunctionName() {
        return RemoveSequenceFlowsFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson, BpmnManagedReference modelReference) {
        Result<AddSequenceFlowsCallParameterDto, String> argumentsParsingResult =
                callArgumentsParser.parseArguments(
                        callArgumentsJson, AddSequenceFlowsCallParameterDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        BpmnModel model = modelReference.getCurrentValue();

        AddSequenceFlowsCallParameterDto callArguments = argumentsParsingResult.getValue();
        var responseMessageBuilder = new StringBuilder();
        for (SequenceFlowDto sequenceFlowDto : callArguments.sequenceFlows()) {
            Optional<String> sourceId = model.findElementByModelFriendlyId(sequenceFlowDto.source());
            if (sourceId.isEmpty()) {
                return Result.error("Element with id '%s' does not exist".formatted(sequenceFlowDto.source()));
            }

            Optional<String> targetId = model.findElementByModelFriendlyId(sequenceFlowDto.target());
            if (targetId.isEmpty()) {
                return Result.error("Element with id '%s' does not exist".formatted(sequenceFlowDto.target()));
            }
            Result<String, AddSequenceFlowError> addSequenceFlowResult = model.addUnlabelledSequenceFlow(
                    sequenceFlowDto.source(),
                    sequenceFlowDto.target()
            );

            if (addSequenceFlowResult.isError()) {
                if (addSequenceFlowResult.getError() == ELEMENTS_ALREADY_CONNECTED) {
                    responseMessageBuilder.append("Elements '%s' and '%s' are already connected".formatted(
                            sequenceFlowDto.source(),
                            sequenceFlowDto.target()
                    ));
                    responseMessageBuilder.append('\n');
                } else {
                    log.warn(
                            "Unexpected error '{}' when adding sequence flow between '{} and '{}'",
                            addSequenceFlowResult.getError(),
                            sequenceFlowDto.source(),
                            sequenceFlowDto.target()
                    );
                }
            }

            responseMessageBuilder.append("Added sequence flow between '%s' and '%s'".formatted(
                    sequenceFlowDto.source(),
                    sequenceFlowDto.target()
            ));
            responseMessageBuilder.append('\n');
        }

        modelReference.setValue(model);

        return Result.ok(responseMessageBuilder.toString());
    }
}
