package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.Graph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
@Slf4j
public class BpmnToGraphExporter {

    public Graph export(BpmnModel model) {
        var graph = Graph.empty();
        String startEventId = model.getStartEvent();
        LinkedHashSet<String> elementsToVisit = new LinkedHashSet<>();
        elementsToVisit.add(startEventId);
        Set<String> visitedElements = new HashSet<>();

        boolean nodeAdded = graph.addNode(startEventId, "Start");
        if (!nodeAdded) {
            log.warn("Start event already has a corresponding node in the graph");
        }

        while (!elementsToVisit.isEmpty()) {
            String currentlyProcessedElement = elementsToVisit.iterator().next();
            elementsToVisit.remove(currentlyProcessedElement);
            visitedElements.add(currentlyProcessedElement);

            for (String elementSuccessorId : model.findSuccessors(currentlyProcessedElement)) {
                if (!graph.containsNodeWithId(elementSuccessorId)) {
                    graph.addNode(elementSuccessorId, model.getModelFriendlyId(elementSuccessorId));
                }
                graph.addEdge(currentlyProcessedElement, elementSuccessorId);
                if (!visitedElements.contains(elementSuccessorId)) {
                    elementsToVisit.add(elementSuccessorId);
                }
            }
        }

        return graph;
    }
}
