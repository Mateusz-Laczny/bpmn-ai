package edu.agh.bpmnai.generator.v2.session;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

@Component
public class SessionStateStore {

    private final List<ChatMessageDto> messages;

    private BpmnModel model;

    public SessionStateStore() {
        messages = new ArrayList<>();
        model = new BpmnModel();
    }

    public void appendMessage(ChatMessageDto message) {
        messages.add(message);
    }

    public List<ChatMessageDto> messages() {
        return unmodifiableList(messages);
    }

    public BpmnModel model() {
        return model;
    }

    public ChatMessageDto lastAddedMessage() {
        return messages.get(messages().size() - 1);
    }

    public int numberOfMessages() {
        return messages.size();
    }

    public void clearState() {
        messages.clear();
        model = new BpmnModel();
    }
}
