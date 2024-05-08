package edu.agh.bpmnai.generator.bpmn.model;

public interface ModelModificationChangelog {

    void nodeAdded(HumanReadableId elementId, BpmnNodeType nodeType);

    void nodeRemoved(HumanReadableId elementId, BpmnNodeType nodeType);

    void sequenceFlowAdded(HumanReadableId sourceId, HumanReadableId targetId);

    void sequenceFlowRemoved(HumanReadableId sourceId, HumanReadableId targetId);

    ModelModificationChangelog copy();

    ChangelogSnapshot getSnapshot();
}
