package edu.agh.bpmnai.generator.openai.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SingleChatResponse(
        Integer index,
        ChatMessage message,
        @JsonProperty("finish_reason")
        String finishReason
) {
}
