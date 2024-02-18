package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import edu.agh.bpmnai.generator.v2.FunctionCallResponseDto;
import edu.agh.bpmnai.generator.v2.WhileLoopDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;

@Service
@Slf4j
public class AddWhileLoopCallExecutor implements FunctionCallExecutor {

    private final ObjectMapper objectMapper;

    @Autowired
    public AddWhileLoopCallExecutor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getFunctionName() {
        return "add_while_loop";
    }

    @Override
    public FunctionCallResult executeCall(SessionState sessionState, String functionId, JsonNode callArguments) {
        WhileLoopDto arguments;
        try {
            arguments = objectMapper.readValue(callArguments.asText(), WhileLoopDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        BpmnModel model = sessionState.model();
        String checkActivityName = arguments.checkActivity();
        Optional<String> optionalCheckActivityElementId = model.findTaskIdByName(checkActivityName);
        String checkActivityElementId;
        Set<String> predecessorSuccessorsBeforeModification;
        if (optionalCheckActivityElementId.isPresent()) {
            checkActivityElementId = optionalCheckActivityElementId.get();
            predecessorSuccessorsBeforeModification = model.findSuccessors(checkActivityElementId);
        } else {
            String checkTaskId = model.addTask(checkActivityName);
            String previousElementId;
            if (arguments.predecessorElement().equals("Start")) {
                previousElementId = model.findStartEvents().iterator().next();
            } else {
                previousElementId = model.findTaskIdByName(arguments.predecessorElement()).get();
            }
            model.addUnlabelledSequenceFlow(previousElementId, checkTaskId);
            predecessorSuccessorsBeforeModification = model.findPredecessors(previousElementId);
            checkActivityElementId = checkTaskId;
        }

        model.clearSuccessors(checkActivityElementId);

        String openingGatewayId = model.addGateway(EXCLUSIVE);
        model.addUnlabelledSequenceFlow(checkActivityElementId, openingGatewayId);
        if (!predecessorSuccessorsBeforeModification.isEmpty()) {
            if (predecessorSuccessorsBeforeModification.size() > 1) {
                log.warn("Predecessor activity has more than on successor, choosing the first one");
            }
            String nextTaskId = predecessorSuccessorsBeforeModification.iterator().next();
            model.addLabelledSequenceFlow(openingGatewayId, nextTaskId, "false");
        }

        String previousElementInLoopId = openingGatewayId;
        for (String taskInLoop : arguments.activitiesInLoop()) {
            String newTaskId = model.addTask(taskInLoop);
            model.addUnlabelledSequenceFlow(previousElementInLoopId, newTaskId);
            previousElementInLoopId = newTaskId;
        }

        model.addUnlabelledSequenceFlow(previousElementInLoopId, checkActivityElementId);
        try {
            String responseContent = objectMapper.writeValueAsString(new FunctionCallResponseDto(true));
            return FunctionCallResult.withResponse(new ChatMessageDto("tool", responseContent, functionId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
