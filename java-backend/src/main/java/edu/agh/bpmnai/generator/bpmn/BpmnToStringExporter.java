package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.DirectedEdge;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

@Service
public class BpmnToStringExporter {

    private final SessionStateStore sessionStateStore;

    public BpmnToStringExporter(SessionStateStore sessionStateStore) {
        this.sessionStateStore = sessionStateStore;
    }

    public String export() {
        var nodeListBuilder = new StringBuilder("Nodes:\n");
        var edgeListBuilder = new StringBuilder("Edges:\n");

        BpmnModel model = sessionStateStore.model();
        Collection<String> flowNodes = model.getFlowNodes();
        if (!flowNodes.isEmpty()) {
            for (String nodeId : flowNodes) {
                String nodeModelInterfaceId = sessionStateStore.getModelInterfaceId(nodeId).orElseThrow();
                String nodeName = model.getName(nodeId).orElseThrow();
                nodeListBuilder.append(new HumanReadableId(nodeName, nodeModelInterfaceId).asString()).append(",\n");
            }
        }

        // Remove trailing newline
        nodeListBuilder.deleteCharAt(nodeListBuilder.length() - 1);

        Set<DirectedEdge> sequenceFlows = model.getSequenceFlows();
        if (!sequenceFlows.isEmpty()) {
            for (DirectedEdge sequenceFlow : sequenceFlows) {
                String sourceModelInterfaceId =
                        sessionStateStore.getModelInterfaceId(sequenceFlow.sourceId()).orElseThrow();
                String targetModelInterfaceId =
                        sessionStateStore.getModelInterfaceId(sequenceFlow.targetId()).orElseThrow();
                String sourceName = model.getName(sequenceFlow.sourceId()).orElseThrow();
                String targetName = model.getName(sequenceFlow.targetId()).orElseThrow();
                edgeListBuilder.append('(')
                        .append(new HumanReadableId(sourceName, sourceModelInterfaceId).asString())
                        .append(')')
                        .append(" -> ")
                        .append('(')
                        .append(new HumanReadableId(targetName, targetModelInterfaceId).asString())
                        .append("),\n");
            }
        }

        // Remove trailing newline
        edgeListBuilder.deleteCharAt(edgeListBuilder.length() - 1);

        return nodeListBuilder + "\n" + edgeListBuilder;
    }
}
