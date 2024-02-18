package edu.agh.bpmnai.generator.v2;

public record ConversationMessage(String author, String content) {

    public static ConversationMessage userMessage(String content) {
        return new ConversationMessage("user", content);
    }

    public static ConversationMessage assistantMessage(String content) {
        return new ConversationMessage("assistant", content);
    }
}
