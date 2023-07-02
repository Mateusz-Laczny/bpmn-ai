package edu.agh.bpmnai.generator.bpmn.model;

public record BpmnMessageEvent(String parentElementId, String messageId, String messageName, String eventId) {
}
