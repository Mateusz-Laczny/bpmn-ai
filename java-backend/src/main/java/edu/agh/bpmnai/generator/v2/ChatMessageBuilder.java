package edu.agh.bpmnai.generator.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageBuilder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatMessageDto buildUserMessage(String content) {
        return ChatMessageDto.userFacingMessage("user", content);
    }

    public ChatMessageDto buildToolCallResponseMessage(String callId, FunctionCallResponseDto response) {
        try {
            String contentAsString = objectMapper.writeValueAsString(response);
            return ChatMessageDto.modelOnlyToolResponse("tool", contentAsString, callId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ChatMessageDto buildSystemMessage(String messageContent) {
        return ChatMessageDto.modelOnlyMessage("system", messageContent);
    }
}
