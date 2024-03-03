package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.parameter.ParallelGatewayDto;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddParallelGatewayCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    AddParallelGatewayCallExecutor executor;

    RetrospectiveSummary aRetrospectiveSummary;

    @BeforeEach
    void setUp() {
        executor = new AddParallelGatewayCallExecutor(new ToolCallArgumentsParser(mapper));
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void works_as_expected() throws JsonProcessingException {
        SessionState sessionState = new SessionState(List.of());
        BpmnModel model = sessionState.model();
        String predecessorTaskId = model.addTask("task");
        ParallelGatewayDto callArguments = new ParallelGatewayDto(aRetrospectiveSummary, "", "elementName", "task", List.of("activity1", "activity2"));

        executor.executeCall(sessionState, "id", mapper.writeValueAsString(callArguments));

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