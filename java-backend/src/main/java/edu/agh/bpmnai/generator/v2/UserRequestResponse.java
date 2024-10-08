package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.bpmn.model.FlowModificationLog;
import edu.agh.bpmnai.generator.bpmn.model.NodeModificationLog;
import jakarta.annotation.Nullable;

import java.util.Set;

public record UserRequestResponse(@Nullable String responseContent,
                                  String bpmnXml,
                                  FinishReason finishReason,
                                  Set<NodeModificationLog> nodeModificationLogs,
                                  Set<FlowModificationLog> flowModificationLogs) {
}
