package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.AddIfElseBranchingFunction;
import edu.agh.bpmnai.generator.v2.functions.ArgumentsParsingResult;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.IfElseBranchingDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;

@Service
@Slf4j
public class AddIfElseBranchingCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    @Autowired
    public AddIfElseBranchingCallExecutor(ToolCallArgumentsParser callArgumentsParser, SessionStateStore sessionStateStore) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
    }

    @Override
    public String getFunctionName() {
        return AddIfElseBranchingFunction.FUNCTION_NAME;
    }

    @Override
    public FunctionCallResult executeCall(String callArgumentsJson) {
        ArgumentsParsingResult<IfElseBranchingDto> argumentsParsingResult = callArgumentsParser.parseArguments(callArgumentsJson, IfElseBranchingDto.class);
        if (argumentsParsingResult.isError()) {
            return FunctionCallResult.unsuccessfulCall(argumentsParsingResult.errors());
        }

        IfElseBranchingDto callArguments = argumentsParsingResult.result();
        BpmnModel model = sessionStateStore.model();
        String checkTaskName = callArguments.checkTask();
        Optional<String> optionalCheckTaskElementId = model.findTaskIdByName(checkTaskName);
        String checkTaskElementId;
        if (optionalCheckTaskElementId.isPresent()) {
            checkTaskElementId = optionalCheckTaskElementId.get();
        } else {
            if (callArguments.predecessorElement() == null) {
                log.info("Check task does not exist in the model and predecessor element is null, tool call cannot proceed");
                return FunctionCallResult.unsuccessfulCall(List.of("Check task does not exist in the model and predecessor element is null"));
            }

            Optional<String> predecessorElementId = model.findTaskIdByName(callArguments.predecessorElement());
            if (predecessorElementId.isEmpty()) {
                log.info("Predecessor element does not exist in the model");
                return FunctionCallResult.unsuccessfulCall(List.of("Predecessor element does not exist in the model"));
            }

            String previousElementId = predecessorElementId.get();

            String checkTaskId = model.addTask(checkTaskName);
            model.addUnlabelledSequenceFlow(previousElementId, checkTaskId);
            checkTaskElementId = checkTaskId;
        }

        model.clearSuccessors(checkTaskElementId);

        String trueBranchBeginningElementId;
        Optional<String> existingTrueBranchBeginningElementId = model.findTaskIdByName(callArguments.trueBranchBeginningTask());
        trueBranchBeginningElementId = existingTrueBranchBeginningElementId.orElseGet(() -> model.addTask(callArguments.trueBranchBeginningTask()));

        String falseBranchBeginningElementId;
        Optional<String> existingFalseBranchBeginningElementId = model.findTaskIdByName(callArguments.falseBranchBeginningTask());
        falseBranchBeginningElementId = existingFalseBranchBeginningElementId.orElseGet(() -> model.addTask(callArguments.falseBranchBeginningTask()));

        String gatewayId = model.addGateway(EXCLUSIVE);
        model.addUnlabelledSequenceFlow(checkTaskElementId, gatewayId);
        model.addLabelledSequenceFlow(gatewayId, trueBranchBeginningElementId, "true");
        model.addLabelledSequenceFlow(gatewayId, falseBranchBeginningElementId, "false");

        return FunctionCallResult.successfulCall();
    }
}
