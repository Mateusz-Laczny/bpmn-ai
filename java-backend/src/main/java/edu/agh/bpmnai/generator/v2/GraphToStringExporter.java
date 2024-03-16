package edu.agh.bpmnai.generator.v2;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GraphToStringExporter {

    public String export(Graph graph) {
        if (graph.isEmpty()) {
            return "Nodes:\n" +
                   "Edges:";
        }

        var nodeListBuilder = new StringBuilder("Nodes: ");
        var edgeListBuilder = new StringBuilder("Edges: ");
        List<Node> nodesToVisit = new ArrayList<>();
        nodesToVisit.add(graph.getNodes().iterator().next());
        Set<Node> visitedNodes = new HashSet<>();
        while (!nodesToVisit.isEmpty()) {
            Node processedNode = nodesToVisit.remove(0);
            visitedNodes.add(processedNode);
            for (Node nodeNeighbour : graph.getNeighboursOfNode(processedNode.id())) {
                edgeListBuilder.append('(').append(processedNode.label()).append(')').append(" -> ").append('(').append(nodeNeighbour.label()).append("), ");
                if (!visitedNodes.contains(nodeNeighbour)) {
                    nodesToVisit.add(nodeNeighbour);
                }
            }
        }

        for (Node node : graph.getAllNodes()) {
            nodeListBuilder.append(node.label()).append(", ");
        }

        return nodeListBuilder + "\n" + edgeListBuilder;
    }
}
