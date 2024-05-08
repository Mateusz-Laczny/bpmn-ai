package edu.agh.bpmnai.generator.bpmn.model;

import java.util.Set;

public record ChangelogSnapshot(Set<NodeModificationLog> nodeModificationLogs,
                                Set<FlowModificationLog> flowModificationLogs) {
}
