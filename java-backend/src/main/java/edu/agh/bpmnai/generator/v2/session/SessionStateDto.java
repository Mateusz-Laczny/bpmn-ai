package edu.agh.bpmnai.generator.v2.session;

import edu.agh.bpmnai.generator.v2.ConversationMessage;

import java.util.List;

import static java.util.Collections.unmodifiableList;

public record SessionStateDto(List<ConversationMessage> messages, String modelXml) {
    public SessionStateDto(List<ConversationMessage> messages, String modelXml) {
        this.messages = unmodifiableList(messages);
        this.modelXml = modelXml;
    }
}
