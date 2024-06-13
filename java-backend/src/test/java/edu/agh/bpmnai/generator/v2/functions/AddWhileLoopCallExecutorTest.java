package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.NodeIdToModelInterfaceIdFunction;
import edu.agh.bpmnai.generator.v2.functions.execution.AddWhileLoopCallExecutor;
import edu.agh.bpmnai.generator.v2.functions.parameter.NullabilityCheck;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.functions.parameter.WhileLoopDto;
import edu.agh.bpmnai.generator.v2.session.ImmutableSessionState;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import edu.agh.bpmnai.generator.v2.session.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.NEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddWhileLoopCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    RetrospectiveSummary aRetrospectiveSummary;
    SessionStateStore sessionStateStore;
    AddWhileLoopCallExecutor executor;
    String aSessionId = "ID";
    SessionStatus aSessionStatus = NEW;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        executor = new AddWhileLoopCallExecutor(
                new ToolCallArgumentsParser(mapper, new NullabilityCheck()),
                sessionStateStore,
                new InsertElementIntoDiagram(new CheckIfValidInsertionPoint()),
                new NodeIdToModelInterfaceIdFunction(),
                new FindInsertionPointForSubprocessWithCheckTask(
                        new CheckIfValidInsertionPoint()
                )
        );
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void should_work_as_expected_for_existing_check_activity() throws JsonProcessingException {
        BpmnModel model = new BpmnModel();
        String checkTaskId = model.addTask("task");
        WhileLoopDto callArguments = new WhileLoopDto(
                aRetrospectiveSummary,
                "someName",
                new HumanReadableId("task", "task").asString(),
                null,
                List.of("task1", "task2")
        );
        var sessionState = ImmutableSessionState.builder()
                .sessionId(aSessionId)
                .sessionStatus(aSessionStatus)
                .model(model)
                .putNodeIdToModelInterfaceId(checkTaskId, "task")
                .build();

        Result<FunctionCallResult, String> executorResult =
                executor.executeCall(mapper.writeValueAsString(callArguments), sessionState);
        assertTrue(executorResult.isOk(), "Result should be OK but is '%s'".formatted(executorResult.getError()));
        BpmnModel modelAfterModification = executorResult.getValue().updatedSessionState().bpmnModel();

        Optional<String> firstTaskId = modelAfterModification.findElementByName("task1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = modelAfterModification.findElementByName("task2");
        assertTrue(secondTaskId.isPresent());

        Set<String> predecessorTaskSuccessors = modelAfterModification.findSuccessors(checkTaskId);
        assertEquals(1, predecessorTaskSuccessors.size());

        String openingGatewayId = predecessorTaskSuccessors.iterator().next();
        Set<String> openingGatewaySuccessors = modelAfterModification.findSuccessors(openingGatewayId);
        assertEquals(2, openingGatewaySuccessors.size());
        assertTrue(openingGatewaySuccessors.contains(firstTaskId.get()));

        assertTrue(modelAfterModification.findSuccessors(firstTaskId.get()).contains(secondTaskId.get()));
        assertTrue(modelAfterModification.findSuccessors(secondTaskId.get()).contains(checkTaskId));
    }

    @Test
    void should_work_as_expected_for_new_check_activity_task() throws JsonProcessingException {
        BpmnModel model = new BpmnModel();
        String predecessorTaskId = model.addTask("task");
        var sessionState = ImmutableSessionState.builder()
                .sessionId(aSessionId)
                .sessionStatus(aSessionStatus)
                .model(model)
                .putNodeIdToModelInterfaceId(predecessorTaskId, "task")
                .build();

        WhileLoopDto callArguments = new WhileLoopDto(
                aRetrospectiveSummary,
                "someName",
                "checkTask",
                "taks#task",
                List.of("task1", "task2")
        );

        Result<FunctionCallResult, String> executorResult =
                executor.executeCall(mapper.writeValueAsString(callArguments), sessionState);
        assertTrue(executorResult.isOk(), "Result should be OK but is '%s'".formatted(executorResult.getError()));
        BpmnModel modelAfterModification = executorResult.getValue().updatedSessionState().bpmnModel();

        Optional<String> checkTaskId = modelAfterModification.findElementByName("checkTask");
        assertTrue(checkTaskId.isPresent());
        Optional<String> firstTaskId = modelAfterModification.findElementByName("task1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = modelAfterModification.findElementByName("task2");
        assertTrue(secondTaskId.isPresent());

        Set<String> predecessorTaskSuccessors = modelAfterModification.findSuccessors(checkTaskId.get());
        assertEquals(1, predecessorTaskSuccessors.size());

        String openingGatewayId = predecessorTaskSuccessors.iterator().next();
        Set<String> openingGatewaySuccessors = modelAfterModification.findSuccessors(openingGatewayId);
        assertEquals(2, openingGatewaySuccessors.size());
        assertTrue(openingGatewaySuccessors.contains(firstTaskId.get()));

        assertTrue(modelAfterModification.findSuccessors(firstTaskId.get()).contains(secondTaskId.get()));
        assertTrue(modelAfterModification.findSuccessors(secondTaskId.get()).contains(checkTaskId.get()));
    }
}