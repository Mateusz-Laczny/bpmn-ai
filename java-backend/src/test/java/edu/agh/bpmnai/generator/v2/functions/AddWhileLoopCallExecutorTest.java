package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.execution.ActivityService;
import edu.agh.bpmnai.generator.v2.functions.execution.AddWhileLoopCallExecutor;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.functions.parameter.WhileLoopDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.v2.functions.parameter.DuplicateHandlingStrategy.ADD_NEW_INSTANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddWhileLoopCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    RetrospectiveSummary aRetrospectiveSummary;
    SessionStateStore sessionStateStore;
    AddWhileLoopCallExecutor executor;

    ActivityService activityService;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        activityService = new ActivityService();
        executor = new AddWhileLoopCallExecutor(
                new ToolCallArgumentsParser(mapper),
                sessionStateStore,
                activityService,
                new InsertElementIntoDiagram()
        );
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void should_work_as_expected_for_existing_check_activity() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();
        String checkTaskId = model.addTask("task", "task");
        WhileLoopDto callArguments = new WhileLoopDto(
                aRetrospectiveSummary,
                "someName",
                "task",
                null,
                List.of(new Activity("task1", ADD_NEW_INSTANCE, false), new Activity("task2", ADD_NEW_INSTANCE, false))
        );

        executor.executeCall(mapper.writeValueAsString(callArguments));

        Optional<String> firstTaskId = model.findElementByModelFriendlyId("task1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = model.findElementByModelFriendlyId("task2");
        assertTrue(secondTaskId.isPresent());

        Set<String> predecessorTaskSuccessors = model.findSuccessors(checkTaskId);
        assertEquals(1, predecessorTaskSuccessors.size());

        String openingGatewayId = predecessorTaskSuccessors.iterator().next();
        Set<String> openingGatewaySuccessors = model.findSuccessors(openingGatewayId);
        assertEquals(1, openingGatewaySuccessors.size());
        assertTrue(openingGatewaySuccessors.contains(firstTaskId.get()));

        assertTrue(model.findSuccessors(firstTaskId.get()).contains(secondTaskId.get()));
        assertTrue(model.findSuccessors(secondTaskId.get()).contains(checkTaskId));
    }

    @Test
    void should_work_as_expected_for_new_check_activity_task() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();
        String predecessorTaskId = model.addTask("task", "task");

        WhileLoopDto callArguments = new WhileLoopDto(
                aRetrospectiveSummary,
                "someName",
                "checkActivity",
                "task",
                List.of(new Activity("task1", ADD_NEW_INSTANCE, false), new Activity("task2", ADD_NEW_INSTANCE, false))
        );

        executor.executeCall(mapper.writeValueAsString(callArguments));

        Optional<String> checkTaskId = model.findElementByModelFriendlyId("checkActivity");
        assertTrue(checkTaskId.isPresent());
        Optional<String> firstTaskId = model.findElementByModelFriendlyId("task1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = model.findElementByModelFriendlyId("task2");
        assertTrue(secondTaskId.isPresent());

        Set<String> predecessorTaskSuccessors = model.findSuccessors(checkTaskId.get());
        assertEquals(1, predecessorTaskSuccessors.size());

        String openingGatewayId = predecessorTaskSuccessors.iterator().next();
        Set<String> openingGatewaySuccessors = model.findSuccessors(openingGatewayId);
        assertEquals(1, openingGatewaySuccessors.size());
        assertTrue(openingGatewaySuccessors.contains(firstTaskId.get()));

        assertTrue(model.findSuccessors(firstTaskId.get()).contains(secondTaskId.get()));
        System.out.println(model.asXmlString());
        System.out.println(model.findSuccessors(secondTaskId.get()));
        assertTrue(model.findSuccessors(secondTaskId.get()).contains(checkTaskId.get()));
    }
}