package edu.agh.bpmnai.generator.bpmn.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BpmnSequenceFlow(
        @JsonProperty(required = true)
        String processId,
        @JsonProperty(required = true)
        String sourceElementId,
        @JsonProperty(required = true)
        String targetElementId,
        String name
) {
}
