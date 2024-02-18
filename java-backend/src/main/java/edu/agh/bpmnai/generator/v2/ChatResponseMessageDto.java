package edu.agh.bpmnai.generator.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ChatResponseMessageDto(String role, String content,
                                     @JsonProperty("tool_calls") List<ToolCallDto> toolCalls) {
}
