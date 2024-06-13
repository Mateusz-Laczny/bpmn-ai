package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.execution.RemoveNodesCallExecutor;
import edu.agh.bpmnai.generator.v2.functions.parameter.NullabilityCheck;
import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveNodesFunctionCallDto;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.session.ImmutableSessionState;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import edu.agh.bpmnai.generator.v2.session.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.NEW;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoveNodesCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    SessionStateStore sessionStateStore;
    RetrospectiveSummary aRetrospectiveSummary;
    String aSessionId = "ID";
    SessionStatus aSessionStatus = NEW;
    private RemoveNodesCallExecutor executor;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        executor = new RemoveNodesCallExecutor(
                new ToolCallArgumentsParser(mapper, new NullabilityCheck()),
                sessionStateStore
        );
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void removes_task_from_the_model() throws JsonProcessingException {
        var model = new BpmnModel();
        String taskId = model.addTask("task");
        var sessionState = ImmutableSessionState.builder()
                .sessionId(aSessionId)
                .sessionStatus(aSessionStatus)
                .bpmnModel(model)
                .putNodeIdToModelInterfaceId(taskId, "task")
                .build();
        RemoveNodesFunctionCallDto callArguments = new RemoveNodesFunctionCallDto(aRetrospectiveSummary, "",
                                                                                  List.of("task#task")
        );

        Result<FunctionCallResult, String> executorResult =
                executor.executeCall(mapper.writeValueAsString(callArguments), sessionState);
        assertTrue(executorResult.isOk(), "Result should be OK but is '%s'".formatted(executorResult.getError()));
        BpmnModel modelAfterModification = executorResult.getValue().updatedSessionState().bpmnModel();

        assertFalse(modelAfterModification.nodeIdExist(taskId));
    }
}