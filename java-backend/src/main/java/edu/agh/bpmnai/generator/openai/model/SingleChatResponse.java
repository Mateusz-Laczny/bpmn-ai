package edu.agh.bpmnai.generator.openai.model;

public record SingleChatResponse(
        Integer index,
        ChatMessage message,
        String finish_reason
) {
}
