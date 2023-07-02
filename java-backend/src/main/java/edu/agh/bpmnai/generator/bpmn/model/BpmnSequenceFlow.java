package edu.agh.bpmnai.generator.bpmn.model;

public record BpmnSequenceFlow(String id, String parentElementId, String sourceRef, String targetRef, String name) {
}
