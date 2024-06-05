package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Nullable;
import lombok.Builder;

@Builder
public record ChatFunctionDto(
        String name,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String description,
        @Nullable
        JsonNode parameters
) {
}
