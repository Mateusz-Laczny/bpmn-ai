package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.execution.AddSequenceOfTasksCallExecutor;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.functions.parameter.SequenceOfTasksDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
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

    RetrospectiveSummary aRetrospectiveSummary;

    SessionStateStore sessionStateStore;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        executor = new AddSequenceOfTasksCallExecutor(new ToolCallArgumentsParser(mapper), sessionStateStore);
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void works_as_expected() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();
        String predecessorTaskId = model.addTask("task");
        SequenceOfTasksDto callArguments = new SequenceOfTasksDto(aRetrospectiveSummary, "", "task", List.of("activity1", "activity2"));

        executor.executeCall(mapper.writeValueAsString(callArguments));

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