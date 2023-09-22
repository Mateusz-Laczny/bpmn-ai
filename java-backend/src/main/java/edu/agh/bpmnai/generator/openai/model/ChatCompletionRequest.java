package edu.agh.bpmnai.generator.openai.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
public final class ChatCompletionRequest {
    private final String model;
    private final List<ChatMessage> messages;
    private final List<ChatFunction> functions;
    private final Object function_call;
    private final Float temperature;
    private final Float top_p;
    private final Float n;
    private final Boolean stream;
    private final Object stop;
    @With
    private final Integer max_tokens;
    private final Float presence_penalty;
    private final Float frequency_penalty;
    private final Map<String, Integer> logit_bias;
    private final String user;

    public ChatCompletionRequest(String model,
                                 List<ChatMessage> messages,
                                 List<ChatFunction> functions,
                                 Float temperature) {
        this(model, messages, functions, null, temperature, null, null, null, null, null, null, null, null, null);
    }

    public ChatCompletionRequest withMessagesAndMax_Tokens(List<ChatMessage> messages, int max_tokens) {
        return new ChatCompletionRequest(
                model,
                messages,
                functions,
                function_call,
                temperature,
                top_p,
                n,
                stream,
                stop,
                max_tokens,
                presence_penalty,
                frequency_penalty,
                logit_bias,
                user
        );
    }
}
