package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.bpmn.model.FlowModificationLog;
import edu.agh.bpmnai.generator.bpmn.model.NodeModificationLog;

import java.util.Set;

public record UserRequestResponse(String responseContent, String bpmnXml, Set<NodeModificationLog> nodeModificationLogs,
                                  Set<FlowModificationLog> flowModificationLogs) {
}
