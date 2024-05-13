package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnNodeType.END_EVENT;
import static org.junit.jupiter.api.Assertions.*;

class ModelPostProcessingTest {
    ModelPostProcessing modelPostProcessing;
    BpmnModel model;
    SessionStateStore sessionStateStore;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        modelPostProcessing = new ModelPostProcessing(
                sessionStateStore,
                new NodeIdToModelInterfaceIdFunction(sessionStateStore)
        );
        model = new BpmnModel();
    }

    @Test
    void removes_gateway_with_single_successor_and_single_predecessor() {
        String predecessor = model.addTask("A");
        sessionStateStore.setModelInterfaceId(predecessor, "A");
        String successor = model.addTask("B");
        sessionStateStore.setModelInterfaceId(successor, "B");
        String gateway = model.addGateway(BpmnGatewayType.EXCLUSIVE, "Gateway");
        sessionStateStore.setModelInterfaceId(gateway, "Gateway");
        model.addUnlabelledSequenceFlow(predecessor, gateway);
        model.addUnlabelledSequenceFlow(gateway, successor);
        sessionStateStore.setModel(model);

        modelPostProcessing.apply();

        BpmnModel afterPostProcessing = sessionStateStore.model();
        assertEquals(Set.of(successor), afterPostProcessing.findSuccessors(predecessor));
        assertFalse(afterPostProcessing.doesIdExist(gateway));
    }

    @Test
    void does_not_remove_gateways_with_more_than_one_predecessor() {
        String predecessor1 = model.addTask("A");
        String predecessor2 = model.addTask("A");
        String successor = model.addTask("B");
        String gateway = model.addGateway(BpmnGatewayType.EXCLUSIVE, "Gateway");
        model.addUnlabelledSequenceFlow(predecessor1, gateway);
        model.addUnlabelledSequenceFlow(predecessor2, gateway);
        model.addUnlabelledSequenceFlow(gateway, successor);
        sessionStateStore.setModel(model);

        modelPostProcessing.apply();

        BpmnModel afterPostProcessing = sessionStateStore.model();
        assertTrue(afterPostProcessing.doesIdExist(gateway));
    }

    @Test
    void adds_end_event_to_elements_without_successors() {
        String task = model.addTask("task");
        sessionStateStore.setModel(model);

        modelPostProcessing.apply();

        BpmnModel afterPostProcessing = sessionStateStore.model();
        LinkedHashSet<String> successorsAfterPostProcessing = afterPostProcessing.findSuccessors(task);
        assertEquals(1, successorsAfterPostProcessing.size());
        assertEquals(END_EVENT, afterPostProcessing.getNodeType(successorsAfterPostProcessing.iterator().next()).get());
    }

    @Test
    void does_not_add_end_event_to_end_event() {
        String endEvent = model.addEndEvent();
        sessionStateStore.setModel(model);

        modelPostProcessing.apply();


        assertEquals(0, sessionStateStore.model().findSuccessors(endEvent).size());
    }
}