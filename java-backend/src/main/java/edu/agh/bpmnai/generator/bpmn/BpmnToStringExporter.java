package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.DirectedEdge;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BpmnToStringExporter {

    public String export(BpmnModel model) {
        var nodeListBuilder = new StringBuilder("Nodes:\n");
        var edgeListBuilder = new StringBuilder("Edges:\n");

        Set<String> flowNodes = model.getFlowNodes();
        if (!flowNodes.isEmpty()) {
            for (String node : flowNodes) {
                nodeListBuilder.append(model.getHumanReadableId(node).orElseThrow().asString()).append(",\n");
            }
        }

        // Remove trailing newline
        nodeListBuilder.deleteCharAt(nodeListBuilder.length() - 1);

        Set<DirectedEdge> sequenceFlows = model.getSequenceFlows();
        if (!sequenceFlows.isEmpty()) {
            for (DirectedEdge sequenceFlow : sequenceFlows) {
                edgeListBuilder.append('(')
                               .append(model.getHumanReadableId(sequenceFlow.sourceId())
                                            .orElseThrow()
                                            .asString())
                               .append(')')
                               .append(" -> ")
                               .append('(')
                               .append(model.getHumanReadableId(sequenceFlow.targetId()).orElseThrow().asString())
                               .append("),\n");
            }
        }

        // Remove trailing newline
        edgeListBuilder.deleteCharAt(nodeListBuilder.length() - 1);

        return nodeListBuilder + "\n" + edgeListBuilder;
    }
}
