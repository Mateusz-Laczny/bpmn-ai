package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BpmnToStringExporterTest {

    SessionStateStore sessionStateStore;

    BpmnToStringExporter bpmnToStringExporter;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        bpmnToStringExporter = new BpmnToStringExporter(sessionStateStore);
    }

    @Test
    void export_result_contains_all_nodes() {
        var model = new BpmnModel();
        String task1 = model.addTask("task1");
        sessionStateStore.setModelInterfaceId(task1, "task1");
        String task2 = model.addTask("task2");
        sessionStateStore.setModelInterfaceId(task2, "task2");
        sessionStateStore.setModel(model);

        String exportResult = bpmnToStringExporter.export().replaceAll("\\s", "");

        assertEquals("Nodes: task1#task1, task2#task2, Edges:".replaceAll("\\s", ""), exportResult);
    }

    @Test
    void export_result_contains_all_edges() {
        var model = new BpmnModel();
        String task1 = model.addTask("task1");
        sessionStateStore.setModelInterfaceId(task1, "task1");
        String task2 = model.addTask("task2");
        sessionStateStore.setModelInterfaceId(task2, "task2");
        String task3 = model.addTask("task3");
        sessionStateStore.setModelInterfaceId(task3, "task3");
        model.addUnlabelledSequenceFlow(task1, task2);
        model.addUnlabelledSequenceFlow(task2, task3);
        sessionStateStore.setModel(model);

        String exportResult = bpmnToStringExporter.export().replaceAll("\\s", "");

        assertEquals(
                ("Nodes: task1#task1, task2#task2, task3#task3, Edges:(task1#task1) -> (task2#task2), (task2#task2) -> "
                 + "(task3#task3),").replaceAll(
                        "\\s",
                        ""
                ),
                exportResult
        );
    }
}