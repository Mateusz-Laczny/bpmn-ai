package edu.agh.bpmnai.generator.v2.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ConversationHistoryStore {
    public List<String> messages = new ArrayList<>();

    public void appendMessage(String messageContent) {
        messages.add(messageContent);
    }

    public void clearMessages() {
        messages.clear();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public Optional<String> getLastMessage() {
        if (messages.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(messages.get(messages.size() - 1));
    }
}
