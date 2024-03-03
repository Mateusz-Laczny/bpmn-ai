package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.functions.parameter.XorGatewayDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddXorGatewayExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private AddXorGatewayExecutor executor;

    RetrospectiveSummary aRetrospectiveSummary;

    @BeforeEach
    void setUp() {
        executor = new AddXorGatewayExecutor(new ToolCallArgumentsParser(mapper));
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void should_work_as_expected_for_existing_check_activity() throws JsonProcessingException {
        SessionState sessionState = new SessionState(List.of());
        BpmnModel model = sessionState.model();
        String checkTaskId = model.addTask("task");
        XorGatewayDto callArguments = new XorGatewayDto(aRetrospectiveSummary, "", "elementName", "task", null, List.of("task1", "task2"));

        executor.executeCall(sessionState, "id", mapper.writeValueAsString(callArguments));

        Optional<String> firstTaskId = model.findTaskIdByName("task1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = model.findTaskIdByName("task2");
        assertTrue(secondTaskId.isPresent());

        Set<String> checkTaskSuccessors = model.findSuccessors(checkTaskId);
        assertEquals(1, checkTaskSuccessors.size());

        String openingGatewayId = checkTaskSuccessors.iterator().next();
        assertTrue(model.findSuccessors(openingGatewayId).contains(firstTaskId.get()));
        assertTrue(model.findSuccessors(openingGatewayId).contains(secondTaskId.get()));

        Set<String> openingGatewaySuccessors = model.findSuccessors(openingGatewayId);
        assertEquals(2, openingGatewaySuccessors.size());
        assertTrue(openingGatewaySuccessors.contains(firstTaskId.get()));
        assertTrue(openingGatewaySuccessors.contains(secondTaskId.get()));

        Set<String> firstTaskSuccessors = model.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = model.findSuccessors(firstTaskId.get());
        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(1, secondTaskSuccessors.size());
        assertEquals(firstTaskSuccessors, secondTaskSuccessors);
    }

    @Test
    void should_work_as_expected_for_new_check_activity_task() throws JsonProcessingException {
        SessionState sessionState = new SessionState(List.of());
        BpmnModel model = sessionState.model();
        model.addTask("task");
        XorGatewayDto callArguments = new XorGatewayDto(aRetrospectiveSummary, "", "elementName", "checkTask", "task", List.of("task1", "task2"));

        executor.executeCall(sessionState, "id", mapper.writeValueAsString(callArguments));

        Optional<String> checkTaskId = model.findTaskIdByName("checkTask");
        assertTrue(checkTaskId.isPresent());
        Optional<String> firstTaskId = model.findTaskIdByName("task1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = model.findTaskIdByName("task2");
        assertTrue(secondTaskId.isPresent());

        Set<String> checkTaskSuccessors = model.findSuccessors(checkTaskId.get());
        assertEquals(1, checkTaskSuccessors.size());

        String openingGatewayId = checkTaskSuccessors.iterator().next();
        assertTrue(model.findSuccessors(openingGatewayId).contains(firstTaskId.get()));
        assertTrue(model.findSuccessors(openingGatewayId).contains(secondTaskId.get()));

        Set<String> openingGatewaySuccessors = model.findSuccessors(openingGatewayId);
        assertEquals(2, openingGatewaySuccessors.size());
        assertTrue(openingGatewaySuccessors.contains(firstTaskId.get()));
        assertTrue(openingGatewaySuccessors.contains(secondTaskId.get()));

        Set<String> firstTaskSuccessors = model.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = model.findSuccessors(firstTaskId.get());
        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(1, secondTaskSuccessors.size());
        assertEquals(firstTaskSuccessors, secondTaskSuccessors);
    }
}