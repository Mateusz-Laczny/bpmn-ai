package edu.agh.bpmnai.generator;

public record ChatUsage(
        Integer prompt_tokens,
        Integer completion_tokens,
        Integer total_tokens
) {
}
