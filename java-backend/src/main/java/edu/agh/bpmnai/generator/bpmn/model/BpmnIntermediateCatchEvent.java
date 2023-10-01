package edu.agh.bpmnai.generator.bpmn.model;

public record BpmnIntermediateCatchEvent(String processId, String name, BpmnIntermediateCatchEventType eventType) {
}
