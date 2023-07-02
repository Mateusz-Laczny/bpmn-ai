package edu.agh.bpmnai.generator.openai.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ChatMessage {
    @JsonProperty("role")
    private MessageRole role;
    @JsonProperty("content")
    private String content;
    @JsonProperty("name")
    private String name;
    @JsonProperty("function_call")
    private JsonNode function_call;

    public ChatMessage() {
    }

    public ChatMessage(
            MessageRole role,
            String content,
            String name,
            JsonNode function_call
    ) {
        this.role = role;
        this.content = content;
        this.name = name;
        this.function_call = function_call;
    }

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

    @JsonProperty("role")
    public MessageRole role() {
        return role;
    }

    @JsonProperty("content")
    public String content() {
        return content;
    }

    @JsonProperty("name")
    public String name() {
        return name;
    }

    @JsonProperty("function_call")
    public JsonNode function_call() {
        return function_call;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFunction_call(JsonNode function_call) {
        this.function_call = function_call;
    }

    public void setRole(MessageRole role) {
        this.role = role;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ChatMessage) obj;
        return Objects.equals(this.role, that.role) &&
                Objects.equals(this.content, that.content) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.function_call, that.function_call);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, content, name, function_call);
    }

    @Override
    public String toString() {
        return "ChatMessage[" +
                "\n\trole=" + role + ", " +
                "\n\tcontent=" + content + ", " +
                "\n\tname=" + name + ", " +
                "\n\tfunction_call=" + function_call + "\n]";
    }

}
