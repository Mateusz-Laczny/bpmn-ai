package edu.agh.bpmnai.generator.v2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.With;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatMessageDto(String role, String content, @Nullable @JsonProperty("tool_call_id") String toolCallId,
                             @Nullable @JsonProperty("tool_calls") List<ToolCallDto> toolCalls,
                             @JsonIgnore @Nullable @With String userFacingContent) {

    public static ChatMessageDto modelOnlyToolResponse(String role, String content, String toolCallId) {
        return new ChatMessageDto(role, content, toolCallId, null, null);
    }

    public static ChatMessageDto userFacingMessage(String role, String content) {
        return new ChatMessageDto(role, content, null, null, content);
    }

    public static ChatMessageDto modelOnlyMessage(String role, String content) {
        return new ChatMessageDto(role, content, null, null, null);
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    public boolean hasUserFacingContent() {
        return userFacingContent != null;
    }
}
