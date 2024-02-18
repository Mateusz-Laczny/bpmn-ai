package edu.agh.bpmnai.generator.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatChoiceDto(
        Integer index,
        ChatMessageDto message,
        @JsonProperty("finish_reason")
        String finishReason
) {
}