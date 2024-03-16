package edu.agh.bpmnai.generator.v2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphToStringExporterTest {

    GraphToStringExporter exporter;

    @BeforeEach
    void setUp() {
        exporter = new GraphToStringExporter();
    }

    @Test
    void returns_empty_node_and_edge_list_for_empty_diagram() {
        var graph = Graph.empty();

        String graphAsString = exporter.export(graph);

        String expectedStringRepresentation = "Nodes:\n" +
                                              "Edges:";
        assertEquals(expectedStringRepresentation, graphAsString);
    }

    @Test
    void includes_existing_nodes_names_in_the_nodes_list() {
        var graph = Graph.empty();
        graph.addNode("node1", "Node1");
        graph.addNode("node2", "Node2");

        String graphAsString = exporter.export(graph);

        String expectedStringRepresentation = "Nodes: Node1, Node2, \n" +
                                              "Edges: ";
        assertEquals(expectedStringRepresentation, graphAsString);
    }

    @Test
    void includes_existing_edges_in_the_edges_list() {
        var graph = Graph.empty();
        graph.addNode("node1", "Node1");
        graph.addNode("node2", "Node2");
        graph.addEdge("node1", "node2");

        String graphAsString = exporter.export(graph);

        String expectedStringRepresentation = "Nodes: Node1, Node2, \n" +
                                              "Edges: (Node1) -> (Node2), ";
        assertEquals(expectedStringRepresentation, graphAsString);
    }
}