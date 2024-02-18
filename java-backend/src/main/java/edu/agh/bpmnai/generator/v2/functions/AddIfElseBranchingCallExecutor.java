package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import edu.agh.bpmnai.generator.v2.FunctionCallResponseDto;
import edu.agh.bpmnai.generator.v2.IfElseBranchingDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;

@Service
@Slf4j
public class AddIfElseBranchingCallExecutor implements FunctionCallExecutor {

    private final ObjectMapper objectMapper;

    @Autowired
    public AddIfElseBranchingCallExecutor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getFunctionName() {
        return "add_if_else_branching";
    }

    @Override
    public FunctionCallResult executeCall(SessionState sessionState, String functionId, JsonNode callArguments) {
        IfElseBranchingDto arguments;
        try {
            arguments = objectMapper.readValue(callArguments.asText(), IfElseBranchingDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        BpmnModel model = sessionState.model();
        String checkActivityName = arguments.checkActivity();
        Optional<String> optionalCheckActivityElementId = model.findTaskIdByName(checkActivityName);
        String checkActivityElementId;
        if (optionalCheckActivityElementId.isPresent()) {
            checkActivityElementId = optionalCheckActivityElementId.get();
        } else {
            String previousElementId;
            if (arguments.predecessorElement().equals("Start")) {
                previousElementId = model.findStartEvents().iterator().next();
            } else {
                previousElementId = model.findTaskIdByName(arguments.predecessorElement()).get();
            }

            String checkTaskId = model.addTask(checkActivityName);
            model.addUnlabelledSequenceFlow(previousElementId, checkTaskId);
            checkActivityElementId = checkTaskId;
        }

        model.clearSuccessors(checkActivityElementId);

        String trueBranchBeginningElementId;
        Optional<String> existingTrueBranchBeginningElementId = model.findTaskIdByName(arguments.trueBranchBeginningActivity());
        trueBranchBeginningElementId = existingTrueBranchBeginningElementId.orElseGet(() -> model.addTask(arguments.trueBranchBeginningActivity()));

        String falseBranchBeginningElementId;
        Optional<String> existingFalseBranchBeginningElementId = model.findTaskIdByName(arguments.falseBranchBeginningActivity());
        falseBranchBeginningElementId = existingFalseBranchBeginningElementId.orElseGet(() -> model.addTask(arguments.falseBranchBeginningActivity()));

        String gatewayId = model.addGateway(EXCLUSIVE);
        model.addUnlabelledSequenceFlow(checkActivityElementId, gatewayId);
        model.addLabelledSequenceFlow(gatewayId, trueBranchBeginningElementId, "true");
        model.addLabelledSequenceFlow(gatewayId, falseBranchBeginningElementId, "false");
        try {
            String responseContent = objectMapper.writeValueAsString(new FunctionCallResponseDto(true));
            return FunctionCallResult.withResponse(new ChatMessageDto("tool", responseContent, functionId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
