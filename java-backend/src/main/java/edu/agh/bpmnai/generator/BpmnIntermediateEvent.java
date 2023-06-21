package edu.agh.bpmnai.generator;

public record BpmnIntermediateEvent(String id, String processId, String name, boolean catchEvent) {
}
