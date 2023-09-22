package edu.agh.bpmnai.generator;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;

import java.util.Collection;
import java.util.List;

public interface ChatConversation {

    void addMessage(ChatMessage message);

    void addMessages(Collection<ChatMessage> messages);

    List<ChatMessage> getMessages();

    ChatMessage getLastMessage();

    ConversationStatus getCurrentConversationStatus();

    void carryOutConversation(BpmnModel bpmnModel);
}
