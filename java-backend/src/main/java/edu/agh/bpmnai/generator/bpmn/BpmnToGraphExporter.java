package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.Graph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class BpmnToGraphExporter {

    public Graph export(BpmnModel model) {
        var graph = Graph.empty();
        String startEventId = model.getStartEvent();
        List<String> elementsToVisit = new ArrayList<>();
        elementsToVisit.add(startEventId);
        Set<String> visitedElements = new HashSet<>();

        boolean nodeAdded = graph.addNode(startEventId, "Start");
        if (!nodeAdded) {
            log.warn("Start event already has a corresponding node in the graph");
        }

        while (!elementsToVisit.isEmpty()) {
            String currentlyProcessedElement = elementsToVisit.remove(0);
            visitedElements.add(currentlyProcessedElement);

            for (String elementSuccessorId : model.findSuccessors(currentlyProcessedElement)) {
                if (visitedElements.contains(elementSuccessorId)) {
                    continue;
                }

                elementsToVisit.add(elementSuccessorId);
                boolean nodeForNeighbourAdded = graph.addNode(elementSuccessorId, model.getName(elementSuccessorId));
                if (!nodeForNeighbourAdded) {
                    log.warn("Node for element with id '{}' already exists in the graph", elementSuccessorId);
                }
                graph.addEdge(currentlyProcessedElement, elementSuccessorId);
            }
        }

        return graph;
    }
}
