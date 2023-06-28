package edu.agh.bpmnai.generator;

public record BpmnSequenceFlow(String id, String parentElementId, String sourceRef, String targetRef, String name) {
}
