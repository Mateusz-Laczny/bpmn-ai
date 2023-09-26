package edu.agh.bpmnai.generator.openai.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class ChatMessage {
    @JsonProperty("role")
    private MessageRole role;
    @JsonProperty("content")
    private String content;
    @JsonProperty("name")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;
    @JsonProperty("function_call")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FunctionCall functionCall;

    public static ChatMessage systemMessage(String content) {
        return new ChatMessage(MessageRole.SYSTEM, content, null, null);
    }

    public static ChatMessage functionMessage(String content, String name) {
        return new ChatMessage(MessageRole.FUNCTION, content, name, null);
    }

    public static ChatMessage assistantMessage(String content) {
        return new ChatMessage(MessageRole.ASSISTANT, content, null, null);
    }

    public static ChatMessage userMessage(String content) {
        return new ChatMessage(MessageRole.USER, content, null, null);
    }
}
