package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveElementDto;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoveActivityCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private RemoveActivityCallExecutor executor;

    RetrospectiveSummary aRetrospectiveSummary;

    @BeforeEach
    void setUp() {
        executor = new RemoveActivityCallExecutor(new ToolCallArgumentsParser(mapper));
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void removes_task_from_the_model() throws JsonProcessingException {
        SessionState sessionState = new SessionState(List.of());
        BpmnModel model = sessionState.model();
        model.addTask("task");
        RemoveElementDto callArguments = new RemoveElementDto(aRetrospectiveSummary, "", "task");

        executor.executeCall(sessionState, "id", mapper.writeValueAsString(callArguments));

        assertTrue(model.findTaskIdByName("task").isEmpty());
    }
}