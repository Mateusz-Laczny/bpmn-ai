package edu.agh.bpmnai.generator.openai.model;

import lombok.Builder;

import java.util.List;

@Builder
public record ChatCompletionResponse(
        String id,
        String object,
        Integer created,
        List<SingleChatResponse> choices,
        ChatUsage usage
) {
}
