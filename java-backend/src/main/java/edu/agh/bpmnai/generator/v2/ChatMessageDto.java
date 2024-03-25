package edu.agh.bpmnai.generator.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatMessageDto(String role, String content, @Nullable @JsonProperty("tool_call_id") String toolCallId,
                             @Nullable @JsonProperty("tool_calls") List<ToolCallDto> toolCalls) {

    public static ChatMessageDto modelOnlyToolResponse(String role, String content, String toolCallId) {
        return new ChatMessageDto(role, content, toolCallId, null);
    }

    public static ChatMessageDto userFacingMessage(String role, String content) {
        return new ChatMessageDto(role, content, null, null);
    }

    public static ChatMessageDto modelOnlyMessage(String role, String content) {
        return new ChatMessageDto(role, content, null, null);
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

}
