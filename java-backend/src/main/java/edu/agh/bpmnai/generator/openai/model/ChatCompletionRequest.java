package edu.agh.bpmnai.generator.openai.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ChatCompletionRequest(
        String model,
        List<ChatMessage> messages,
        List<ChatFunction> functions,
        Object function_call,
        Float temperature,
        Float top_p,
        Float n,
        Boolean stream,
        Object stop,
        Integer max_tokens,
        Float presence_penalty,
        Float frequency_penalty,
        Map<String, Integer> logit_bias,
        String user
) {
}
