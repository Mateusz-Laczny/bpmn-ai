package edu.agh.bpmnai.generator.bpmn.model;

import java.util.HashSet;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.ModificationType.ADD;
import static edu.agh.bpmnai.generator.bpmn.model.ModificationType.REMOVE;

public class LoggingModelModificationChangelog
        implements ModelModificationChangelog {

    private final Set<NodeModificationLog> nodeModificationLogs;

    private final Set<FlowModificationLog> flowModificationLogs;

    private int nextIndex = 0;

    public LoggingModelModificationChangelog() {
        nodeModificationLogs = new HashSet<>();
        flowModificationLogs = new HashSet<>();
    }

    private LoggingModelModificationChangelog(
            Set<NodeModificationLog> nodeModificationLogs,
            Set<FlowModificationLog> flowModificationLogs
    ) {
        this.nodeModificationLogs = nodeModificationLogs;
        this.flowModificationLogs = flowModificationLogs;
    }

    @Override
    public void nodeAdded(HumanReadableId elementId, BpmnNodeType nodeType) {
        nodeModificationLogs.add(new NodeModificationLog(nextIndex, ADD, elementId, nodeType.asString()));
        nextIndex += 1;
    }

    @Override
    public void nodeRemoved(HumanReadableId elementId, BpmnNodeType nodeType) {
        nodeModificationLogs.add(new NodeModificationLog(nextIndex, REMOVE, elementId, nodeType.asString()));
        nextIndex += 1;
    }

    @Override
    public void sequenceFlowAdded(HumanReadableId sourceId, HumanReadableId targetId) {
        flowModificationLogs.add(new FlowModificationLog(nextIndex, ADD, sourceId, targetId));
        nextIndex += 1;
    }

    @Override
    public void sequenceFlowRemoved(HumanReadableId sourceId, HumanReadableId targetId) {
        flowModificationLogs.add(new FlowModificationLog(nextIndex, REMOVE, sourceId, targetId));
        nextIndex += 1;
    }

    public Set<NodeModificationLog> getModificationLogs() {
        return new HashSet<>(nodeModificationLogs);
    }

    public Set<FlowModificationLog> getFlowModificationLogs() {
        return new HashSet<>(flowModificationLogs);
    }

    @Override
    public ModelModificationChangelog copy() {
        return new LoggingModelModificationChangelog(
                new HashSet<>(nodeModificationLogs),
                new HashSet<>(flowModificationLogs)
        );
    }

    @Override
    public ChangelogSnapshot getSnapshot() {
        return new ChangelogSnapshot(Set.copyOf(nodeModificationLogs), Set.copyOf(flowModificationLogs));
    }
}
