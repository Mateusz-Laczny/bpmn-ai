package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BpmnToGraphExporterTest {

    BpmnToGraphExporter exporter;

    @BeforeEach
    void setUp() {
        exporter = new BpmnToGraphExporter();
    }

    @Test
    void adds_all_existing_elements_as_graph_nodes() {
        var model = new BpmnModel();
        String startEventId = model.getStartEvent();
        String activityId = model.addTask("Some task");
        model.addUnlabelledSequenceFlow(startEventId, activityId);

        Graph resultGraph = exporter.export(model);

        assertTrue(resultGraph.findNodeById(startEventId).isPresent());
        assertTrue(resultGraph.findNodeById(activityId).isPresent());
    }

    @Test
    void does_not_add_disjoint_elements_to_the_graph() {
        var model = new BpmnModel();
        String startEventId = model.getStartEvent();
        String activityId = model.addTask("Some task");

        Graph resultGraph = exporter.export(model);

        assertTrue(resultGraph.findNodeById(startEventId).isPresent());
        assertFalse(resultGraph.findNodeById(activityId).isPresent());
    }
}