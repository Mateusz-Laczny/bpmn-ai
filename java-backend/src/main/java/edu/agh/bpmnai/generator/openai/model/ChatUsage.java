package edu.agh.bpmnai.generator.openai.model;

public record ChatUsage(
        Integer prompt_tokens,
        Integer completion_tokens,
        Integer total_tokens
) {
}
