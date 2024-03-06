package edu.agh.bpmnai.generator.v2.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import edu.agh.bpmnai.generator.v2.FunctionCallResponseDto;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.NEW;
import static java.util.Collections.unmodifiableList;

@ToString
public class SessionState {

    private final List<ChatMessageDto> messages;

    private final List<String> systemMessages;

    private final BpmnModel model;

    private final ObjectMapper objectMapper;

    @Setter
    private SessionStatus sessionStatus;

    public SessionState(List<String> systemMessages) {
        this.systemMessages = new ArrayList<>(systemMessages);
        this.objectMapper = new ObjectMapper();
        messages = new ArrayList<>();
        model = new BpmnModel();
        sessionStatus = NEW;
    }

    public List<String> systemMessages() {
        return unmodifiableList(systemMessages);
    }

    public List<ChatMessageDto> messages() {
        return unmodifiableList(messages);
    }

    public ChatMessageDto getLastMessage() {
        return messages.get(messages.size() - 1);
    }

    public SessionStatus sessionStatus() {
        return sessionStatus;
    }

    public BpmnModel model() {
        return model;
    }

    public void appendAssistantMessage(String messageContent) {
        var assistantMessage = new ChatMessageDto("assistant", messageContent);
        messages.add(assistantMessage);
    }

    public void appendUserMessage(String content) {
        var userMessage = new ChatMessageDto("user", content);
        messages.add(userMessage);
    }

    public void appendMessage(ChatMessageDto message) {
        messages.add(message);
    }

    public void appendToolResponse(String callId, FunctionCallResponseDto response) {
        try {
            String contentAsString = objectMapper.writeValueAsString(response);
            this.messages.add(new ChatMessageDto("tool", contentAsString, callId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
