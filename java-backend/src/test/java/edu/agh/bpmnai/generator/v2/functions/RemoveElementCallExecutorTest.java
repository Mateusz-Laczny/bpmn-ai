package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.execution.RemoveElementCallExecutor;
import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveElementDto;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoveElementCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    SessionStateStore sessionStateStore;

    RetrospectiveSummary aRetrospectiveSummary;
    private RemoveElementCallExecutor executor;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        executor = new RemoveElementCallExecutor(new ToolCallArgumentsParser(mapper), sessionStateStore);
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void removes_task_from_the_model() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();
        model.addTask("task", "");
        RemoveElementDto callArguments = new RemoveElementDto(aRetrospectiveSummary, "", "task");

        executor.executeCall(mapper.writeValueAsString(callArguments));

        assertTrue(model.findElementByModelFriendlyId("task").isEmpty());
    }
}