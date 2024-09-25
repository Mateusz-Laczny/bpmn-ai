package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.session.ImmutableSessionState;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BpmnToStringExporterTest {

    SessionStateStore sessionStateStore;
    BpmnToStringExporter bpmnToStringExporter;
    String aSessionId = "ID";

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        bpmnToStringExporter = new BpmnToStringExporter();
    }

    @Test
    void export_result_contains_all_nodes() {
        var model = new BpmnModel();
        String task1 = model.addTask("task1");
        String task2 = model.addTask("task2");
        var sessionState = ImmutableSessionState.builder()
                .apiKey("123")
                .sessionId(aSessionId)
                .model(model)
                .putNodeIdToModelInterfaceId(task1, "task1")
                .putNodeIdToModelInterfaceId(task2, "task2")
                .build();

        String exportResult = bpmnToStringExporter.export(sessionState).replaceAll("\\s", "");

        assertEquals("Nodes: task1#task1, task2#task2, Edges:".replaceAll("\\s", ""), exportResult);
    }

    @Test
    void export_result_contains_all_edges() {
        var model = new BpmnModel();
        String task1 = model.addTask("task1");
        String task2 = model.addTask("task2");
        String task3 = model.addTask("task3");
        model.addUnlabelledSequenceFlow(task1, task2);
        model.addUnlabelledSequenceFlow(task2, task3);
        var sessionState = ImmutableSessionState.builder()
                .apiKey("123")
                .sessionId(aSessionId)
                .model(model)
                .putNodeIdToModelInterfaceId(task1, "task1")
                .putNodeIdToModelInterfaceId(task2, "task2")
                .putNodeIdToModelInterfaceId(task3, "task3")
                .build();

        String exportResult = bpmnToStringExporter.export(sessionState).replaceAll("\\s", "");

        assertThat(exportResult)
                .contains("Nodes:")
                .contains("task1#task1")
                .contains("task2#task2")
                .contains("task3#task3")
                .contains("Edges:")
                .contains("(task1#task1)->(task2#task2)")
                .contains("(task2#task2)->(task3#task3");
    }
}