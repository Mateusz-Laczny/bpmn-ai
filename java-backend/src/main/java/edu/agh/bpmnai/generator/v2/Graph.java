package edu.agh.bpmnai.generator.v2;

import java.util.*;

import static java.util.stream.Collectors.toSet;

public class Graph {

    private final Map<String, Set<String>> nodeToNeighbours = new HashMap<>();

    private final Map<String, String> nodeLabels = new HashMap<>();

    public static Graph empty() {
        return new Graph();
    }

    public boolean addNode(String nodeId, String label) {
        if (nodeToNeighbours.containsKey(nodeId)) {
            return false;
        }

        nodeToNeighbours.put(nodeId, new HashSet<>());
        setLabel(nodeId, label);
        return true;
    }

    public boolean setLabel(String nodeId, String label) {
        if (!nodeToNeighbours.containsKey(nodeId)) {
            return false;
        }

        nodeLabels.put(nodeId, label);
        return true;
    }

    public void addEdge(String from, String to) {
        if (!nodeToNeighbours.containsKey(from) || !nodeToNeighbours.containsKey(to)) {
            return;
        }

        nodeToNeighbours.get(from).add(to);
    }

    public Set<Node> getNodes() {
        return nodeToNeighbours.keySet().stream()
                .map(nodeId -> new Node(nodeId, nodeLabels.get(nodeId)))
                .collect(toSet());
    }

    public Set<Node> getNeighboursOfNode(String nodeId) {
        return nodeToNeighbours.get(nodeId).stream()
                .map(neighbourNodeId -> new Node(neighbourNodeId, nodeLabels.get(neighbourNodeId)))
                .collect(toSet());
    }

    public boolean isEmpty() {
        return nodeToNeighbours.isEmpty();
    }

    public Set<Node> getAllNodes() {
        return nodeToNeighbours.keySet().stream()
                .map(nodeId -> new Node(nodeId, nodeLabels.get(nodeId)))
                .collect(toSet());
    }

    public Optional<Node> findNodeById(String nodeId) {
        if (!nodeToNeighbours.containsKey(nodeId)) {
            return Optional.empty();
        }

        return Optional.of(new Node(nodeId, nodeLabels.get(nodeId)));
    }
}
