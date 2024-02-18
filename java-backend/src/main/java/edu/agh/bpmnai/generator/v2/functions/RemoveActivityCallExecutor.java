package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import edu.agh.bpmnai.generator.v2.FunctionCallResponseDto;
import edu.agh.bpmnai.generator.v2.RemoveActivityDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RemoveActivityCallExecutor implements FunctionCallExecutor {

    private final ObjectMapper objectMapper;

    @Autowired
    public RemoveActivityCallExecutor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getFunctionName() {
        return "remove_activity";
    }

    @Override
    public FunctionCallResult executeCall(SessionState sessionState, String functionId, JsonNode callArguments) {
        RemoveActivityDto arguments;
        try {
            arguments = objectMapper.readValue(callArguments.asText(), RemoveActivityDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        BpmnModel model = sessionState.model();
        Optional<String> elementToCutOutId = model.findTaskIdByName(arguments.activityToRemove());

        elementToCutOutId.ifPresent(model::cutOutElement);
        try {
            String responseContent = objectMapper.writeValueAsString(new FunctionCallResponseDto(true));
            return FunctionCallResult.withResponse(new ChatMessageDto("tool", responseContent, functionId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
