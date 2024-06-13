package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.NodeIdToModelInterfaceIdFunction;
import edu.agh.bpmnai.generator.v2.functions.execution.AddXorGatewayCallExecutor;
import edu.agh.bpmnai.generator.v2.functions.parameter.NullabilityCheck;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.functions.parameter.Task;
import edu.agh.bpmnai.generator.v2.functions.parameter.XorGatewayDto;
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

class AddXorGatewayCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    RetrospectiveSummary aRetrospectiveSummary;
    SessionStateStore sessionStateStore;
    AddXorGatewayCallExecutor executor;
    String aSessionId = "ID";
    SessionStatus aSessionStatus = NEW;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        var checkIfValidInsertionPoint = new CheckIfValidInsertionPoint();
        executor = new AddXorGatewayCallExecutor(
                new ToolCallArgumentsParser(mapper, new NullabilityCheck()),
                sessionStateStore,
                new InsertElementIntoDiagram(checkIfValidInsertionPoint),
                new NodeIdToModelInterfaceIdFunction(),
                new FindInsertionPointForSubprocessWithCheckTask(checkIfValidInsertionPoint)
        );
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void should_work_as_expected_for_existing_check_activity() throws JsonProcessingException {
        var model = new BpmnModel();
        String checkTaskId = model.addTask("task");
        String checkTaskPredecessorId = model.addTask("checkTaskPredecessor");
        model.addUnlabelledSequenceFlow(checkTaskPredecessorId, checkTaskId);
        var sessionState = ImmutableSessionState.builder()
                .sessionId(aSessionId)
                .sessionStatus(aSessionStatus)
                .model(model)
                .putNodeIdToModelInterfaceId(checkTaskId, "task")
                .putNodeIdToModelInterfaceId(checkTaskPredecessorId, "checkTaskPredecessor")
                .build();
        XorGatewayDto callArguments = new XorGatewayDto(
                aRetrospectiveSummary,
                "",
                "elementName",
                new HumanReadableId("task", "task").asString(),
                null,
                List.of(new Task("task1", false), new Task("task2", false))
        );

        Result<FunctionCallResult, String> functionCallResult =
                executor.executeCall(mapper.writeValueAsString(callArguments), sessionState);
        assertTrue(functionCallResult.isOk());
        BpmnModel modelAfterModification = functionCallResult.getValue().updatedSessionState().bpmnModel();

        Optional<String> firstTaskId = modelAfterModification.findElementByName("task1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = modelAfterModification.findElementByName("task2");
        assertTrue(secondTaskId.isPresent());

        Set<String> checkTaskSuccessors = modelAfterModification.findSuccessors(checkTaskId);
        assertEquals(1, checkTaskSuccessors.size());

        String openingGatewayId = checkTaskSuccessors.iterator().next();
        assertTrue(modelAfterModification.findSuccessors(openingGatewayId).contains(firstTaskId.get()));
        assertTrue(modelAfterModification.findSuccessors(openingGatewayId).contains(secondTaskId.get()));

        Set<String> openingGatewaySuccessors = modelAfterModification.findSuccessors(openingGatewayId);
        assertEquals(2, openingGatewaySuccessors.size());
        assertTrue(openingGatewaySuccessors.contains(firstTaskId.get()));
        assertTrue(openingGatewaySuccessors.contains(secondTaskId.get()));

        Set<String> firstTaskSuccessors = modelAfterModification.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = modelAfterModification.findSuccessors(secondTaskId.get());
        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(1, secondTaskSuccessors.size());
        assertEquals(firstTaskSuccessors, secondTaskSuccessors);

        String closingGatewayId = modelAfterModification.findSuccessors(firstTaskId.get()).iterator().next();
        assertTrue(modelAfterModification.findSuccessors(closingGatewayId).isEmpty());
    }

    @Test
    void should_work_as_expected_for_new_check_activity_task() throws JsonProcessingException {
        var model = new BpmnModel();
        String taskId = model.addTask("task");
        var sessionState = ImmutableSessionState.builder()
                .sessionId(aSessionId)
                .sessionStatus(aSessionStatus)
                .model(model)
                .putNodeIdToModelInterfaceId(taskId, "task")
                .build();
        XorGatewayDto callArguments = new XorGatewayDto(
                aRetrospectiveSummary,
                "",
                "elementName",
                "checkTask",
                "taks#task",
                List.of(new Task("task1", false), new Task("task2", false))
        );

        Result<FunctionCallResult, String> functionCallResult =
                executor.executeCall(mapper.writeValueAsString(callArguments), sessionState);
        assertTrue(functionCallResult.isOk());
        BpmnModel modelAfterModification = functionCallResult.getValue().updatedSessionState().bpmnModel();

        Optional<String> checkTaskId = modelAfterModification.findElementByName("checkTask");
        assertTrue(checkTaskId.isPresent());
        Optional<String> firstTaskId = modelAfterModification.findElementByName("task1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = modelAfterModification.findElementByName("task2");
        assertTrue(secondTaskId.isPresent());

        Set<String> checkTaskSuccessors = modelAfterModification.findSuccessors(checkTaskId.get());
        assertEquals(1, checkTaskSuccessors.size());

        String openingGatewayId = checkTaskSuccessors.iterator().next();
        assertTrue(modelAfterModification.findSuccessors(openingGatewayId).contains(firstTaskId.get()));
        assertTrue(modelAfterModification.findSuccessors(openingGatewayId).contains(secondTaskId.get()));

        Set<String> openingGatewaySuccessors = modelAfterModification.findSuccessors(openingGatewayId);
        assertEquals(2, openingGatewaySuccessors.size());
        assertTrue(openingGatewaySuccessors.contains(firstTaskId.get()));
        assertTrue(openingGatewaySuccessors.contains(secondTaskId.get()));

        Set<String> firstTaskSuccessors = modelAfterModification.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = modelAfterModification.findSuccessors(firstTaskId.get());
        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(1, secondTaskSuccessors.size());
        assertEquals(firstTaskSuccessors, secondTaskSuccessors);
    }

    @Test
    void should_work_as_expected_when_inserting_between_existing_tasks() throws JsonProcessingException {
        var model = new BpmnModel();
        String predecessorTaskId = model.addTask("predecessorTask");
        String successorTaskId = model.addTask("successorTask");
        model.addUnlabelledSequenceFlow(predecessorTaskId, successorTaskId);
        var sessionState = ImmutableSessionState.builder()
                .sessionId(aSessionId)
                .sessionStatus(aSessionStatus)
                .model(model)
                .putNodeIdToModelInterfaceId(predecessorTaskId, "predecessorTask")
                .putNodeIdToModelInterfaceId(successorTaskId, "successorTask")
                .build();
        XorGatewayDto callArguments = new XorGatewayDto(
                aRetrospectiveSummary,
                "",
                "elementName",
                "checkTask",
                "predecessorTask#predecessorTask",
                List.of(new Task("task1", false), new Task("task2", false))
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

        Set<String> checkTaskSuccessors = modelAfterModification.findSuccessors(checkTaskId.get());
        assertEquals(1, checkTaskSuccessors.size());

        Set<String> predecessorTaskSuccessors = modelAfterModification.findSuccessors(predecessorTaskId);
        assertEquals(Set.of(checkTaskId.get()), predecessorTaskSuccessors);

        String openingGatewayId = checkTaskSuccessors.iterator().next();
        assertEquals(
                Set.of(firstTaskId.get(), secondTaskId.get()),
                modelAfterModification.findSuccessors(openingGatewayId)
        );

        Set<String> openingGatewaySuccessors = modelAfterModification.findSuccessors(openingGatewayId);
        assertEquals(2, openingGatewaySuccessors.size());
        assertTrue(openingGatewaySuccessors.contains(firstTaskId.get()));
        assertTrue(openingGatewaySuccessors.contains(secondTaskId.get()));

        Set<String> firstTaskSuccessors = modelAfterModification.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = modelAfterModification.findSuccessors(firstTaskId.get());
        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(1, secondTaskSuccessors.size());
        assertEquals(firstTaskSuccessors, secondTaskSuccessors);

        String closingGatewayId = firstTaskSuccessors.iterator().next();
        Set<String> closingGatewaySuccessors = modelAfterModification.findSuccessors(closingGatewayId);
        assertEquals(1, closingGatewaySuccessors.size());
        assertTrue(closingGatewaySuccessors.contains(successorTaskId));
    }
}