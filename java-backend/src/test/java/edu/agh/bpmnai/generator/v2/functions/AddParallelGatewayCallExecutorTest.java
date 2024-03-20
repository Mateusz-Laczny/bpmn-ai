package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.execution.ActivityService;
import edu.agh.bpmnai.generator.v2.functions.execution.AddParallelGatewayCallExecutor;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
import edu.agh.bpmnai.generator.v2.functions.parameter.ParallelGatewayDto;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.v2.functions.parameter.DuplicateHandlingStrategy.ADD_NEW_INSTANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddParallelGatewayCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    AddParallelGatewayCallExecutor executor;

    RetrospectiveSummary aRetrospectiveSummary;

    SessionStateStore sessionStateStore;

    ActivityService activityService;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        activityService = new ActivityService();
        executor = new AddParallelGatewayCallExecutor(new ToolCallArgumentsParser(mapper), sessionStateStore, activityService);
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void works_as_expected() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();
        String predecessorTaskId = model.addTask("task", "task");
        ParallelGatewayDto callArguments = new ParallelGatewayDto(aRetrospectiveSummary,
                "",
                "elementName",
                "task",
                List.of(new Activity("activity1", ADD_NEW_INSTANCE), new Activity("activity2", ADD_NEW_INSTANCE)));

        executor.executeCall(mapper.writeValueAsString(callArguments));

        Optional<String> firstTaskId = model.findElementByModelFriendlyId("activity1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = model.findElementByModelFriendlyId("activity2");
        assertTrue(secondTaskId.isPresent());

        Set<String> predecessorTaskSuccessors = model.findSuccessors(predecessorTaskId);
        assertEquals(1, predecessorTaskSuccessors.size());
        String openingGatewayId = predecessorTaskSuccessors.iterator().next();
        Set<String> openingGatewaySuccessors = model.findSuccessors(openingGatewayId);
        assertTrue(openingGatewaySuccessors.contains(firstTaskId.get()));
        assertTrue(openingGatewaySuccessors.contains(secondTaskId.get()));

        Set<String> firstTaskSuccessors = model.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = model.findSuccessors(secondTaskId.get());

        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(1, secondTaskSuccessors.size());
        assertEquals(firstTaskSuccessors, secondTaskSuccessors);
    }
}