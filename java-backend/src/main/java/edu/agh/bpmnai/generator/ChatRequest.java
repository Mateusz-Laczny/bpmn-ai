package edu.agh.bpmnai.generator;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatRequest(
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
    public ChatRequest(String model,
                       List<ChatMessage> messages,
                       List<ChatFunction> functions,
                       Float temperature,
                       Integer max_tokens)
    {
        this(model, messages, functions, null, temperature, null, null, null, null, max_tokens, null, null, null, null);
    }
}
