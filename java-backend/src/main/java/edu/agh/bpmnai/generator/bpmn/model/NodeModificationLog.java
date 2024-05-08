package edu.agh.bpmnai.generator.bpmn.model;

public record NodeModificationLog(int index, ModificationType modificationType, HumanReadableId nodeId,
                                  String elementType) {
}

