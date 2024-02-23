package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.RemoveActivityDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoveActivityCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private RemoveActivityCallExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new RemoveActivityCallExecutor(new ToolCallArgumentsParser(mapper));
    }

    @Test
    void removes_task_from_the_model() {
        SessionState sessionState = new SessionState(List.of());
        BpmnModel model = sessionState.model();
        model.addTask("task");
        RemoveActivityDto callArguments = new RemoveActivityDto("", "task");
        JsonNode callArgumentsJson = mapper.valueToTree(callArguments);

        executor.executeCall(sessionState, "id", callArgumentsJson);

        assertTrue(model.findTaskIdByName("task").isEmpty());
    }
}