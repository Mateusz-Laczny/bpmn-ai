package edu.agh.bpmnai.generator.openai.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FunctionCallDto(String name, @JsonProperty("arguments") String argumentsJson) {
}
