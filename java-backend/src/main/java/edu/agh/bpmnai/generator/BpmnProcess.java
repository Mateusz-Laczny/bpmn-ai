package edu.agh.bpmnai.generator;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BpmnProcess(String name, String id) {
}
