package edu.agh.bpmnai.generator.bpmn.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BpmnSequenceFlow(
        @JsonProperty(required = true)
        String id,
        @JsonProperty(required = true)
        String parentElementId,
        @JsonProperty(required = true)
        String sourceElementId,
        @JsonProperty(required = true)
        String targetElementId,
        String name
) {
}
