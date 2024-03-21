package edu.agh.bpmnai.generator.bpmn.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;
import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.PARALLEL;
import static org.junit.jupiter.api.Assertions.*;

class BpmnModelTest {

    @Test
    void correctly_adds_task_to_the_model() {
        BpmnModel model = new BpmnModel();

        String taskId = model.addTask("task", "taskModelFacingName");

        Optional<String> taskIdFromModel = model.findElementByModelFriendlyId("taskModelFacingName");
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

        String taskId = model.addTask("task", "");
        model.setPositionOfElement(taskId, 10, 15);

        Dimensions taskDimensions = model.getElementDimensions(taskId);
        assertEquals(10, taskDimensions.x());
        assertEquals(15, taskDimensions.y());
    }

    @Test
    void correctly_clears_successors_of_the_element() {
        BpmnModel model = new BpmnModel();

        String taskId = model.addTask("task", "taskModelFacingName");
        String secondTaskId = model.addTask("task2", "task2ModelFacingName");
        model.addUnlabelledSequenceFlow(taskId, secondTaskId);

        model.clearSuccessors(taskId);
        assertEquals(Set.of(), model.findSuccessors(taskId));
    }

    @Test
    void getName_returns_correct_name_for_task() {
        BpmnModel model = new BpmnModel();
        String taskId = model.addTask("task", "taskModelFacingName");

        String taskName = model.getModelFacingName(taskId);
        assertEquals("taskModelFacingName", taskName);
    }

    @Test
    void getName_returns_correct_name_for_gateway() {
        BpmnModel model = new BpmnModel();
        String taskId = model.addGateway(EXCLUSIVE, "gateway");

        String taskName = model.getModelFacingName(taskId);
        assertEquals("gateway", taskName);
    }

    @Test
    void areElementsDirectlyConnected_returns_true_if_elements_are_connected() {
        BpmnModel model = new BpmnModel();
        String taskId = model.addTask("", "task");
        model.addUnlabelledSequenceFlow(model.getStartEvent(), taskId);

        assertTrue(model.areElementsDirectlyConnected(model.getStartEvent(), taskId));
    }

    @Test
    void areElementsDirectlyConnected_returns_false_if_elements_are_not_connected() {
        BpmnModel model = new BpmnModel();
        String taskId = model.addTask("", "task");

        assertFalse(model.areElementsDirectlyConnected(model.getStartEvent(), taskId));
    }
}