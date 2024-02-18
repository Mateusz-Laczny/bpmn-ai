package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.ParallelForkDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddParallelActivitiesForkCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();


    @Test
    void works_as_expected() {
        SessionState sessionState = new SessionState(List.of());
        BpmnModel model = sessionState.model();
        String predecessorTaskId = model.addTask("task");
        ParallelForkDto callArguments = new ParallelForkDto("", "task", List.of("activity1", "activity2"));
        JsonNode callArgumentsJson = mapper.valueToTree(callArguments);
        var executor = new AddParallelActivitiesForkCallExecutor(mapper);

        executor.executeCall(sessionState, "id", callArgumentsJson);

        Optional<String> firstTaskId = model.findTaskIdByName("activity1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = model.findTaskIdByName("activity2");
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