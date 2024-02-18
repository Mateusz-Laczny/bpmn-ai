package edu.agh.bpmnai.generator.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatMessageDto(String role, String content, @Nullable @JsonProperty("tool_call_id") String toolCallId,
                             @Nullable @JsonProperty("tool_calls") List<ToolCallDto> toolCalls) {
    public ChatMessageDto(String role, String content) {
        this(role, content, null, null);
    }

    public ChatMessageDto(String role, String content, String toolCallId) {
        this(role, content, toolCallId, null);
    }
}
