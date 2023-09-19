package edu.agh.bpmnai.generator.openai.model;

import java.util.List;

public record ChatCompletionResponse(
        String id,
        String object,
        Integer created,
        List<SingleChatResponse> choices,
        ChatUsage usage
) {
}
