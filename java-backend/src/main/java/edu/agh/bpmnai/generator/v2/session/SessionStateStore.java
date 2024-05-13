package edu.agh.bpmnai.generator.v2.session;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;

@Component
public class SessionStateStore {

    private final List<ChatMessageDto> messages = new ArrayList<>();

    private final BiMap<String, String> elementIdToModelInterfaceId = HashBiMap.create();

    @Setter
    private BpmnModel model = new BpmnModel();

    public void appendMessage(ChatMessageDto message) {
        messages.add(message);
    }

    public List<ChatMessageDto> messages() {
        return unmodifiableList(messages);
    }

    public BpmnModel model() {
        return model.getCopy();
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
        model.addLabelledStartEvent("Start");
        elementIdToModelInterfaceId.clear();
        elementIdToModelInterfaceId.put(model.getStartEvent(), "start-event");
    }

    public Optional<String> getModelInterfaceId(String elementId) {
        return Optional.ofNullable(elementIdToModelInterfaceId.get(elementId));
    }

    public void setModelInterfaceId(String elementId, String modelInterfaceId) {
        elementIdToModelInterfaceId.put(elementId, modelInterfaceId);
    }

    public void removeModelInterfaceId(String elementId) {
        elementIdToModelInterfaceId.remove(elementId);
    }

    public Optional<String> getElementId(String modelInterfaceId) {
        return Optional.ofNullable(elementIdToModelInterfaceId.inverse().get(modelInterfaceId));
    }
}
