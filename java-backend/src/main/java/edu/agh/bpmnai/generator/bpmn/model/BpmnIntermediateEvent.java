package edu.agh.bpmnai.generator.bpmn.model;

public record BpmnIntermediateEvent(String id, String processId, String name, boolean catchEvent) {
}
