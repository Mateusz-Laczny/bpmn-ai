package edu.agh.bpmnai.generator;

public record BpmnSequenceFlow(String id, String processId, String sourceRef, String targetRef, String name) {
}
