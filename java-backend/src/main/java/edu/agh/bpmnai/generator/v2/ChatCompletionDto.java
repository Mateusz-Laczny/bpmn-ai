package edu.agh.bpmnai.generator.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
@ToString
public class ChatCompletionDto {
    private final String model;
    private final List<ChatMessageDto> messages;
    @Nullable
    private final List<ChatToolDto> tools;
    @JsonProperty("tool_choice")
    private final Object toolChoice;
    @Nullable
    private final Double temperature;
}
