package edu.agh.bpmnai.generator;

public record SingleChatResponse(
        Integer index,
        ChatMessage message,
        String finish_reason
) {
}
