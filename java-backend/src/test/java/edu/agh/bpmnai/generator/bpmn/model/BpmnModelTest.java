package edu.agh.bpmnai.generator.bpmn.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.INCLUSIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BpmnModelTest {

    @Test
    void correctly_adds_task_to_the_model() {
        BpmnModel model = new BpmnModel();

        String taskId = model.addTask("task");

        Optional<String> taskIdFromModel = model.findTaskIdByName("task");
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

        String gatewayId = model.addGateway(INCLUSIVE);

        assertTrue(model.doesIdExist(gatewayId));
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
        model.setPositionOfElement(taskId, 10, 15);

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
}