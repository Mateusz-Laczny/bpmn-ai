package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static edu.agh.bpmnai.generator.v2.functions.parameter.DuplicateHandlingStrategy.ADD_NEW_INSTANCE;
import static edu.agh.bpmnai.generator.v2.functions.parameter.DuplicateHandlingStrategy.USE_EXISTING;
import static org.junit.jupiter.api.Assertions.*;

class ActivityServiceTest {

    ActivityService activityService;

    @BeforeEach
    void setUp() {
        activityService = new ActivityService();
    }

    @Test
    void adds_new_instance_if_no_instance_exists() {
        var model = new BpmnModel();

        Result<ActivityIdAndName, String> addResult = activityService.addActivityToModel(
                model,
                new Activity(
                        "anActivity",
                        ADD_NEW_INSTANCE,
                        false
                )
        );

        assertTrue(addResult.isOk());
        assertTrue(model.findElementByModelFriendlyId(addResult.getValue().modelFacingName()).isPresent());
    }

    @Test
    void adds_new_instance_even_if_activity_with_given_name_exists_for_add_new_instance_strategy() {
        var model = new BpmnModel();
        String activityId = model.addTask("anActivity", "anActivity");

        Result<ActivityIdAndName, String> addResult = activityService.addActivityToModel(
                model,
                new Activity(
                        "anActivity",
                        ADD_NEW_INSTANCE,
                        false
                )
        );

        assertTrue(addResult.isOk());
        assertTrue(model.findElementByModelFriendlyId(addResult.getValue().modelFacingName()).isPresent());
        assertNotEquals(activityId, model.findElementByModelFriendlyId(addResult.getValue().modelFacingName()).get());
    }

    @Test
    void adds_new_instance_when_element_does_not_exist_for_use_existing_strategy() {
        var model = new BpmnModel();

        Result<ActivityIdAndName, String> addResult = activityService.addActivityToModel(
                model,
                new Activity(
                        "anActivity",
                        USE_EXISTING,
                        false
                )
        );

        assertTrue(addResult.isOk());
        assertTrue(model.findElementByModelFriendlyId(addResult.getValue().modelFacingName()).isPresent());
    }

    @Test
    void returns_existing_instance_when_element_exist_for_use_existing_strategy() {
        var model = new BpmnModel();
        String activityId = model.addTask("anActivity", "anActivityNameForModel");

        Result<ActivityIdAndName, String> addResult = activityService.addActivityToModel(
                model,
                new Activity(
                        "anActivityNameForModel",
                        USE_EXISTING,
                        false
                )
        );

        assertTrue(addResult.isOk());
        assertEquals(activityId, addResult.getValue().id());
    }
}