package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.openai.model.ChatUsage;
import lombok.Builder;

import java.util.List;

@Builder
public record ChatCompletionResponseDto(
        String id,
        String object,
        Integer created,
        List<ChatChoiceDto> choices,
        ChatUsage usage
) {
}