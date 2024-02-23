package edu.agh.bpmnai.generator.openai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import java.util.Optional;
import java.util.function.Function;

@Builder
public record ChatFunction(
        String name,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String description,
        JsonNode parameters,
        @JsonIgnore
        Function<String, Optional<ChatMessage>> executor
) {
}
