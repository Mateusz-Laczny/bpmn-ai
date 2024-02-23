package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.SequenceOfActivitiesDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddSequenceOfTasksCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private AddSequenceOfTasksCallExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new AddSequenceOfTasksCallExecutor(new ToolCallArgumentsParser(mapper));
    }

    @Test
    void works_as_expected() {
        SessionState sessionState = new SessionState(List.of());
        BpmnModel model = sessionState.model();
        String predecessorTaskId = model.addTask("task");
        SequenceOfActivitiesDto callArguments = new SequenceOfActivitiesDto("", "task", List.of("activity1", "activity2"));
        JsonNode callArgumentsJson = mapper.valueToTree(callArguments);

        executor.executeCall(sessionState, "id", callArgumentsJson);

        Optional<String> firstTaskId = model.findTaskIdByName("activity1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = model.findTaskIdByName("activity2");
        assertTrue(secondTaskId.isPresent());

        Set<String> predecessorTaskSuccessors = model.findSuccessors(predecessorTaskId);
        assertEquals(1, predecessorTaskSuccessors.size());
        assertTrue(predecessorTaskSuccessors.contains(firstTaskId.get()));

        Set<String> firstTaskSuccessors = model.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = model.findSuccessors(secondTaskId.get());

        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(0, secondTaskSuccessors.size());
        assertTrue(firstTaskSuccessors.contains(secondTaskId.get()));
    }
}