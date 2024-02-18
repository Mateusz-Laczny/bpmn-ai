package edu.agh.bpmnai.generator.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.agh.bpmnai.generator.openai.model.FunctionCallDto;

public record ToolCallDto(String id, String type, @JsonProperty("function") FunctionCallDto functionCallProperties) {
}
