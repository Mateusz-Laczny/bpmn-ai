package edu.agh.bpmnai.generator;

public record BpmnMessageEvent(String parentElementId, String messageId, String messageName, String eventId) {
}
