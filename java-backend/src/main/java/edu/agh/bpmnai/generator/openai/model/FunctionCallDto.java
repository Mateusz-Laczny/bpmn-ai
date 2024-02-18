package edu.agh.bpmnai.generator.openai.model;

import com.fasterxml.jackson.databind.JsonNode;

public record FunctionCallDto(String name, JsonNode arguments) {
}
