package edu.agh.bpmnai.generator;

import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
class ChatConversation {
    private final List<ChatMessage> messages;
    private ConversationStatus status;

    private ChatConversation(List<ChatMessage> messages, ConversationStatus status) {
        this.messages = new ArrayList<>(messages);
        this.status = status;
    }

    public static ChatConversation emptyConversation() {
        return new ChatConversation(new ArrayList<>(), ConversationStatus.NEW);
    }

    public void addMessage(ChatMessage message) {
        this.messages.add(message);
    }

    public void addMessages(Collection<ChatMessage> messages) {
        this.messages.addAll(messages);
    }

    public List<ChatMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public ConversationStatus getStatus() {
        return status;
    }

    public void setStatus(ConversationStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ChatConversation) obj;
        return Objects.equals(this.messages, that.messages) &&
                Objects.equals(this.status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messages, status);
    }

    @Override
    public String toString() {
        return "ChatConversation[" +
                "messages=" + messages + ",\n" +
                "status=" + status + ']';
    }

}
