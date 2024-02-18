package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import edu.agh.bpmnai.generator.v2.FunctionCallResponseDto;
import edu.agh.bpmnai.generator.v2.SequenceOfActivitiesDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class AddSequenceOfTasksCallExecutor implements FunctionCallExecutor {

    private final ObjectMapper objectMapper;

    @Autowired
    public AddSequenceOfTasksCallExecutor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getFunctionName() {
        return "add_sequence_of_activities";
    }

    @Override
    public FunctionCallResult executeCall(SessionState sessionState, String functionId, JsonNode callArgumentsJson) {
        SequenceOfActivitiesDto callArguments;
        try {
            callArguments = objectMapper.readValue(callArgumentsJson.asText(), SequenceOfActivitiesDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        BpmnModel model = sessionState.model();
        String previousElementId;
        if (callArguments.predecessorElement().equals("Start")) {
            previousElementId = model.findStartEvents().iterator().next();
        } else {
            previousElementId = model.findTaskIdByName(callArguments.predecessorElement()).get();
        }

        model.clearSuccessors(previousElementId);

        Set<String> predecessorTaskSuccessorsBeforeModification = model.findSuccessors(previousElementId);
        if (predecessorTaskSuccessorsBeforeModification.size() > 1) {
            log.warn("Predecessor activity has more than one successor, choosing the first one; activityName: {}", callArguments.predecessorElement());
        }

        for (String newActivityName : callArguments.newActivities()) {
            String nextTaskId = model.findTaskIdByName(newActivityName).orElseGet(() -> model.addTask(newActivityName));
            if (model.findSuccessors(previousElementId).contains(nextTaskId)) {
                continue;
            }

            model.addUnlabelledSequenceFlow(previousElementId, nextTaskId);
            previousElementId = nextTaskId;
        }

        if (!predecessorTaskSuccessorsBeforeModification.isEmpty()) {
            if (predecessorTaskSuccessorsBeforeModification.size() > 1) {
                log.warn("Predecessor element has more than one successor, choosing the first one; activityName: {}", callArguments.predecessorElement());
            }

            String endOfChainElementId = predecessorTaskSuccessorsBeforeModification.iterator().next();
            model.addUnlabelledSequenceFlow(previousElementId, endOfChainElementId);
        }
        try {
            String responseContent = objectMapper.writeValueAsString(new FunctionCallResponseDto(true));
            return FunctionCallResult.withResponse(new ChatMessageDto("tool", responseContent, functionId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
