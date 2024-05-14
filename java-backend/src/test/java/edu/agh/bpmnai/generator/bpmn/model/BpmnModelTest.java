package edu.agh.bpmnai.generator.bpmn.model;

import edu.agh.bpmnai.generator.bpmn.layouting.Point2d;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;
import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.PARALLEL;
import static edu.agh.bpmnai.generator.bpmn.model.BpmnNodeType.PARALLEL_GATEWAY;
import static edu.agh.bpmnai.generator.bpmn.model.BpmnNodeType.XOR_GATEWAY;
import static org.junit.jupiter.api.Assertions.*;

class BpmnModelTest {

    @Test
    void correctly_adds_task_to_the_model() {
        BpmnModel model = new BpmnModel();

        String taskId = model.addTask("task");

        Optional<String> taskIdFromModel = model.findElementByName("task");
        assertTrue(taskIdFromModel.isPresent());
        assertEquals(taskId, taskIdFromModel.get());
        Dimensions taskDimensions = model.getElementDimensions(taskId);
        assertEquals(0, taskDimensions.x());
        assertEquals(0, taskDimensions.y());
        assertEquals(83, taskDimensions.width());
        assertEquals(68, taskDimensions.height());
    }

    @Test
    void correctly_adds_gateway_to_the_model() {
        BpmnModel model = new BpmnModel();

        String gatewayId = model.addGateway(PARALLEL, "some name");

        assertTrue(model.nodeIdExist(gatewayId));
        Dimensions taskDimensions = model.getElementDimensions(gatewayId);
        assertEquals(0, taskDimensions.x());
        assertEquals(0, taskDimensions.y());
        assertEquals(42, taskDimensions.width());
        assertEquals(42, taskDimensions.height());
    }

    @Test
    void correctly_updates_the_position_of_the_element() {
        BpmnModel model = new BpmnModel();

        String taskId = model.addTask("task");
        model.setPositionOfElement(taskId, new Point2d(10, 15));

        Dimensions taskDimensions = model.getElementDimensions(taskId);
        assertEquals(10, taskDimensions.x());
        assertEquals(15, taskDimensions.y());
    }

    @Test
    void correctly_clears_successors_of_the_element() {
        BpmnModel model = new BpmnModel();

        String taskId = model.addTask("task");
        String secondTaskId = model.addTask("task2");
        model.addUnlabelledSequenceFlow(taskId, secondTaskId);

        model.clearSuccessors(taskId);
        assertEquals(Set.of(), model.findSuccessors(taskId));
    }

    @Test
    void getName_returns_correct_name_for_task() {
        BpmnModel model = new BpmnModel();
        String taskId = model.addTask("task");

        String taskName = model.getName(taskId).orElseThrow();
        assertEquals("task", taskName);
    }

    @Test
    void getName_returns_correct_name_for_gateway() {
        BpmnModel model = new BpmnModel();
        String taskId = model.addGateway(EXCLUSIVE, "gateway");

        String taskName = model.getName(taskId).orElseThrow();
        assertEquals("gateway", taskName);
    }

    @Test
    void areElementsDirectlyConnected_returns_true_if_elements_are_connected() {
        BpmnModel model = new BpmnModel();
        String task1Id = model.addTask("task1");
        String task2Id = model.addTask("task2");
        model.addUnlabelledSequenceFlow(task1Id, task2Id);

        assertTrue(model.areElementsDirectlyConnected(task1Id, task2Id));
    }

    @Test
    void areElementsDirectlyConnected_returns_false_if_elements_are_not_connected() {
        BpmnModel model = new BpmnModel();
        String taskId = model.addTask("");
        String task2Id = model.addTask("");

        assertFalse(model.areElementsDirectlyConnected(taskId, task2Id));
    }

    @Test
    void finds_successors_connected_with_labelled_sequence_flows() {
        BpmnModel model = new BpmnModel();

        String taskId = model.addTask("task");
        String secondTaskId = model.addTask("task2");
        model.addLabelledSequenceFlow(taskId, secondTaskId, "label");

        assertEquals(Set.of(secondTaskId), model.findSuccessors(taskId));
    }

    @Test
    void findElementsOfType_returns_all_gateways() {
        BpmnModel model = new BpmnModel();
        String gatewayId = model.addGateway(EXCLUSIVE, "gateway");
        String anotherGatewayId = model.addGateway(PARALLEL, "anotherGateway");

        assertEquals(Set.of(gatewayId), model.findElementsOfType(XOR_GATEWAY));
        assertEquals(Set.of(anotherGatewayId), model.findElementsOfType(PARALLEL_GATEWAY));
    }

    @Test
    void removes_the_element() {
        BpmnModel model = new BpmnModel();
        String task = model.addTask("a");
        model.removeFlowNode(task);
        assertFalse(model.nodeIdExist(task));
    }
}