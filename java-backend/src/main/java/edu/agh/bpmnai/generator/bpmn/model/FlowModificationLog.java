package edu.agh.bpmnai.generator.bpmn.model;

public record FlowModificationLog(int index, ModificationType modificationType, HumanReadableId sourceId,
                                  HumanReadableId targetId) {
}
