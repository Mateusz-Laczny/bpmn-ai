package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import edu.agh.bpmnai.generator.v2.FunctionCallResponseDto;
import edu.agh.bpmnai.generator.v2.UserDescriptionReasoningDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class IsDescriptionDetailedEnoughCallExecutor implements FunctionCallExecutor {

    private final ObjectMapper objectMapper;

    @Autowired
    public IsDescriptionDetailedEnoughCallExecutor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getFunctionName() {
        return "is_request_description_detailed_enough";
    }

    @Override
    public FunctionCallResult executeCall(SessionState sessionState, String functionId, JsonNode callArguments) {
        UserDescriptionReasoningDto arguments;
        try {
            arguments = objectMapper.readValue(callArguments.asText(), UserDescriptionReasoningDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (arguments.messageToTheUser() != null) {
            sessionState.onMessageReceivedOneTime((message, sessionStateAfterResponse) -> {
                try {
                    String responseContent = objectMapper.writeValueAsString(new FunctionCallResponseDto(true, Map.of("response", message.content())));
                    sessionStateAfterResponse.insertMessage(new ChatMessageDto("tool", responseContent, functionId), sessionStateAfterResponse.getNumberOfMessages() - 1);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
            return new FunctionCallResult(Optional.of(new ChatMessageDto("assistant", arguments.messageToTheUser())), true);
        }

        try {
            String responseContent = objectMapper.writeValueAsString(new FunctionCallResponseDto(true));
            return FunctionCallResult.withResponse(new ChatMessageDto("tool", responseContent, functionId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
