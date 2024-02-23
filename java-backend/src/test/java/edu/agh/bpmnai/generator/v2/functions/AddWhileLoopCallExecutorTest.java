package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.WhileLoopDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddWhileLoopCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private AddWhileLoopCallExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new AddWhileLoopCallExecutor(new ToolCallArgumentsParser(mapper));
    }

    @Test
    void should_work_as_expected_for_existing_check_activity() {
        SessionState sessionState = new SessionState(List.of());
        BpmnModel model = sessionState.model();
        String checkTaskId = model.addTask("task");
        WhileLoopDto callArguments = new WhileLoopDto("", "task", null, List.of("task1", "task2"));
        JsonNode callArgumentsJson = mapper.valueToTree(callArguments);

        executor.executeCall(sessionState, "id", callArgumentsJson);

        Optional<String> firstTaskId = model.findTaskIdByName("task1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = model.findTaskIdByName("task2");
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
    void should_work_as_expected_for_new_check_activity_task() {
        SessionState sessionState = new SessionState(List.of());
        BpmnModel model = sessionState.model();
        model.addTask("task");

        WhileLoopDto callArguments = new WhileLoopDto("", "checkTask", "task", List.of("task1", "task2"));
        JsonNode callArgumentsJson = mapper.valueToTree(callArguments);

        executor.executeCall(sessionState, "id", callArgumentsJson);

        Optional<String> checkTaskId = model.findTaskIdByName("checkTask");
        assertTrue(checkTaskId.isPresent());
        Optional<String> firstTaskId = model.findTaskIdByName("task1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = model.findTaskIdByName("task2");
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