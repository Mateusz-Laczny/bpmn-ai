package edu.agh.bpmnai.generator.v2.functions.execution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.AddSequenceFlowsCallParameterDto;
import edu.agh.bpmnai.generator.v2.functions.parameter.NullabilityCheck;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.functions.parameter.SequenceFlowDto;
import edu.agh.bpmnai.generator.v2.session.ImmutableSessionState;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AddSequenceFlowsCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    SessionStateStore sessionStateStore;
    AddSequenceFlowsCallExecutor executor;
    String aSessionId = "ID";

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        executor = new AddSequenceFlowsCallExecutor(
                new ToolCallArgumentsParser(mapper, new NullabilityCheck())
        );
    }

    @Test
    void correctly_adds_sequence_flow() throws JsonProcessingException {
        var model = new BpmnModel();
        String task1Id = model.addTask("task1");
        String task2Id = model.addTask("task2");
        var sessionState = ImmutableSessionState.builder()
                .sessionId(aSessionId)
                .bpmnModel(model)
                .putNodeIdToModelInterfaceId(task1Id, "task1")
                .putNodeIdToModelInterfaceId(task2Id, "task2")
                .build();
        var callArguments = new AddSequenceFlowsCallParameterDto(new RetrospectiveSummary(""), "",
                                                                 Set.of(new SequenceFlowDto(
                                                                         "task1#task1",
                                                                         "task2#task2"
                                                                 ))
        );

        Result<FunctionCallResult, String> executorResult =
                executor.executeCall(mapper.writeValueAsString(callArguments), sessionState);
        assertTrue(executorResult.isOk(), "Result should be OK but is '%s'".formatted(executorResult.getError()));
        BpmnModel modelAfterModification = executorResult.getValue().updatedSessionState().bpmnModel();

        assertTrue(modelAfterModification.areElementsDirectlyConnected(task1Id, task2Id));
    }
}