package edu.agh.bpmnai.generator.v2.functions.execution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.NullabilityCheck;
import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveSequenceFlowsCallParameterDto;
import edu.agh.bpmnai.generator.v2.functions.parameter.SequenceFlowDto;
import edu.agh.bpmnai.generator.v2.session.ImmutableSessionState;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import edu.agh.bpmnai.generator.v2.session.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.NEW;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoveSequenceFlowsCallExecutorTest {

    SessionStateStore sessionStateStore;
    RemoveSequenceFlowsCallExecutor executor;
    ObjectMapper objectMapper = new ObjectMapper();
    String aSessionId = "ID";
    SessionStatus aSessionStatus = NEW;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        executor = new RemoveSequenceFlowsCallExecutor(new ToolCallArgumentsParser(
                objectMapper,
                new NullabilityCheck()
        ), sessionStateStore);
    }

    @Test
    void removes_existing_sequence_flow() throws JsonProcessingException {
        var model = new BpmnModel();
        String task1Id = model.addTask("task1");
        String task2Id = model.addTask("task2");
        model.addUnlabelledSequenceFlow(task1Id, task2Id);
        var sessionState = ImmutableSessionState.builder()
                .sessionId(aSessionId)
                .sessionStatus(aSessionStatus)
                .model(model)
                .putNodeIdToModelInterfaceId(task1Id, "task-1")
                .putNodeIdToModelInterfaceId(task2Id, "task-2")
                .build();
        RemoveSequenceFlowsCallParameterDto callArguments =
                new RemoveSequenceFlowsCallParameterDto(List.of(new SequenceFlowDto(
                        "task1#task-1",
                        "task2#task-2"
                )));
        Result<FunctionCallResult, String> callExecutionResult =
                executor.executeCall(objectMapper.writeValueAsString(callArguments), sessionState);

        assertTrue(
                callExecutionResult.isOk(),
                "Call execution result should be ok but is '%s'".formatted(callExecutionResult.getError())
        );

        BpmnModel modelAfterCallExecution = callExecutionResult.getValue().updatedSessionState().bpmnModel();

        assertFalse(
                modelAfterCallExecution.areElementsDirectlyConnected(task1Id, task2Id),
                modelAfterCallExecution.asXmlString()
        );
    }
}