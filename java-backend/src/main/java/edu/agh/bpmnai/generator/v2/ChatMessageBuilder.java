package edu.agh.bpmnai.generator.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageBuilder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatMessageDto buildAssistantMessage(String messageContent) {
        return new ChatMessageDto("assistant", messageContent);
    }

    public ChatMessageDto buildUserMessage(String content) {
        return new ChatMessageDto("user", content);
    }

    public ChatMessageDto buildToolCallResponseMessage(String callId, FunctionCallResponseDto response) {
        try {
            String contentAsString = objectMapper.writeValueAsString(response);
            return new ChatMessageDto("tool", contentAsString, callId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ChatMessageDto buildSystemMessage(String messageContent) {
        return new ChatMessageDto("system", messageContent);
    }
}
