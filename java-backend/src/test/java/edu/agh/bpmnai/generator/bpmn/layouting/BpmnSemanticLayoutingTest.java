package edu.agh.bpmnai.generator.bpmn.layouting;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.Dimensions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BpmnSemanticLayoutingTest {

    @Test
    void if_element_has_single_successor_inserts_it_in_the_same_row_and_next_column() {
        int cellWidth = 100;
        int cellHeight = 100;
        var layouter = new BpmnSemanticLayouting(cellWidth, cellHeight);
        var model = new BpmnModel();
        String taskId = model.addTask("aTask", "");
        String startEventId = model.getStartEvent();
        model.addUnlabelledSequenceFlow(startEventId, taskId);

        BpmnModel layoutedModel = layouter.layoutModel(model);
        Dimensions startEventDimensions = layoutedModel.getElementDimensions(startEventId);
        assertEquals(0, startEventDimensions.x());
        assertEquals(0, startEventDimensions.y());

        Dimensions taskDimensions = layoutedModel.getElementDimensions(taskId);
        assertEquals(cellWidth, taskDimensions.x());
        assertEquals(0, taskDimensions.y());
    }

    @Test
    void connected_elements_are_still_connected_after_layouting() {
        int cellWidth = 100;
        int cellHeight = 100;
        var layouter = new BpmnSemanticLayouting(cellWidth, cellHeight);
        var model = new BpmnModel();
        String taskId = model.addTask("aTask", "aTask");
        String startEventId = model.getStartEvent();
        model.addUnlabelledSequenceFlow(startEventId, taskId);

        BpmnModel layoutedModel = layouter.layoutModel(model);
        assertEquals(Set.of(taskId), layoutedModel.findSuccessors(startEventId));
    }

    @Test
    void if_element_has_multiple_successors_inserts_them_in_different_rows_in_the_next_column() {
        int cellWidth = 100;
        int cellHeight = 100;
        var layouter = new BpmnSemanticLayouting(cellWidth, cellHeight);
        var model = new BpmnModel();
        String firstTaskId = model.addTask("aTask1", "aTask1");
        String secondTaskId = model.addTask("aTask2", "aTask2");
        String startEventId = model.getStartEvent();
        model.addUnlabelledSequenceFlow(startEventId, firstTaskId);
        model.addUnlabelledSequenceFlow(startEventId, secondTaskId);

        BpmnModel layoutedModel = layouter.layoutModel(model);

        Dimensions firstTaskDimensions = layoutedModel.getElementDimensions(firstTaskId);
        assertEquals(cellWidth, firstTaskDimensions.x());
        assertEquals(0, firstTaskDimensions.y());

        Dimensions secondTaskDimensions = layoutedModel.getElementDimensions(secondTaskId);
        assertEquals(cellWidth, secondTaskDimensions.x());
        assertEquals(cellHeight * 2, secondTaskDimensions.y());

        Dimensions startEventDimensions = layoutedModel.getElementDimensions(startEventId);
        assertEquals(0, startEventDimensions.x());
        assertEquals(cellHeight, startEventDimensions.y());
    }

    @Test
    void does_not_change_the_position_of_already_visited_elements() {
        int cellWidth = 100;
        int cellHeight = 100;
        var layouter = new BpmnSemanticLayouting(cellWidth, cellHeight);
        var model = new BpmnModel();
        String taskId = model.addTask("aTask1", "aTask1");
        String startEventId = model.getStartEvent();
        model.addUnlabelledSequenceFlow(startEventId, taskId);
        model.addUnlabelledSequenceFlow(taskId, startEventId);

        BpmnModel layoutedModel = layouter.layoutModel(model);

        Dimensions taskDimensions = layoutedModel.getElementDimensions(taskId);
        assertEquals(cellWidth, taskDimensions.x());
        assertEquals(0, taskDimensions.y());

        Dimensions startEventDimensions = layoutedModel.getElementDimensions(startEventId);
        assertEquals(0, startEventDimensions.x());
        assertEquals(0, startEventDimensions.y());
    }

    @Test
    void if_element_has_multiple_predecessors_inserts_it_between_them() {
        int cellWidth = 100;
        int cellHeight = 100;
        var layouter = new BpmnSemanticLayouting(cellWidth, cellHeight);
        var model = new BpmnModel();
        String firstTaskId = model.addTask("aTask1", "aTask1");
        String secondTaskId = model.addTask("aTask2", "aTask2");

        String startEventId = model.getStartEvent();
        model.addUnlabelledSequenceFlow(startEventId, firstTaskId);
        model.addUnlabelledSequenceFlow(startEventId, secondTaskId);

        String successorTaskId = model.addTask("successorTask", "successorTask");
        model.addUnlabelledSequenceFlow(firstTaskId, successorTaskId);
        model.addUnlabelledSequenceFlow(secondTaskId, successorTaskId);

        BpmnModel layoutedModel = layouter.layoutModel(model);

        Dimensions startEventDimensions = layoutedModel.getElementDimensions(startEventId);
        Dimensions successorTaskDimensions = layoutedModel.getElementDimensions(successorTaskId);
        assertEquals(startEventDimensions.y(), successorTaskDimensions.y());
    }
}