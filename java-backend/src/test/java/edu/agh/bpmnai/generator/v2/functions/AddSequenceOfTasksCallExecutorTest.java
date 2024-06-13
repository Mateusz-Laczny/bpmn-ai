package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.NodeIdToModelInterfaceIdFunction;
import edu.agh.bpmnai.generator.v2.functions.execution.AddSequenceOfTasksCallExecutor;
import edu.agh.bpmnai.generator.v2.functions.parameter.NullabilityCheck;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.functions.parameter.SequenceOfTasksDto;
import edu.agh.bpmnai.generator.v2.session.ImmutableSessionState;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddSequenceOfTasksCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    RetrospectiveSummary aRetrospectiveSummary;
    SessionStateStore sessionStateStore;
    AddSequenceOfTasksCallExecutor executor;
    String aSessionId = "ID";

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        executor = new AddSequenceOfTasksCallExecutor(
                new ToolCallArgumentsParser(mapper, new NullabilityCheck()),
                sessionStateStore,
                new InsertElementIntoDiagram(new CheckIfValidInsertionPoint()),
                new NodeIdToModelInterfaceIdFunction()
        );
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void works_as_expected() throws JsonProcessingException {
        var model = new BpmnModel();
        String predecessorTaskId = model.addTask("task");
        var sessionState = ImmutableSessionState.builder()
                .sessionId(aSessionId)
                .bpmnModel(model)
                .putNodeIdToModelInterfaceId(predecessorTaskId, "task")
                .build();

        SequenceOfTasksDto callArguments = new SequenceOfTasksDto(
                aRetrospectiveSummary,
                "",
                "task#task",
                List.of(
                        "activity1",
                        "activity2"
                )
        );

        Result<FunctionCallResult, String> executorResult =
                executor.executeCall(mapper.writeValueAsString(callArguments), sessionState);
        assertTrue(executorResult.isOk(), "Result should be OK but is '%s'".formatted(executorResult.getError()));
        BpmnModel modelAfterModification = executorResult.getValue().updatedSessionState().bpmnModel();

        Optional<String> firstTaskId = modelAfterModification.findElementByName("activity1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = modelAfterModification.findElementByName("activity2");
        assertTrue(secondTaskId.isPresent());

        Set<String> predecessorTaskSuccessors = modelAfterModification.findSuccessors(predecessorTaskId);
        assertEquals(1, predecessorTaskSuccessors.size());
        assertTrue(predecessorTaskSuccessors.contains(firstTaskId.get()));

        Set<String> firstTaskSuccessors = modelAfterModification.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = modelAfterModification.findSuccessors(secondTaskId.get());

        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(0, secondTaskSuccessors.size());
        assertTrue(firstTaskSuccessors.contains(secondTaskId.get()));
    }

    @Test
    void works_as_expected_when_inserting_the_sequence_into_an_existing_model() throws JsonProcessingException {
        var model = new BpmnModel();
        model.addLabelledStartEvent("Start");
        String checkTaskId = model.addTask("task");
        String gatewayId = model.addGateway(EXCLUSIVE, "gateway");
        String firstPathAfterGateway = model.addTask("path1");
        String secondPathAfterGateway = model.addTask("path2");
        model.addUnlabelledSequenceFlow(model.getStartEvent(), checkTaskId);
        model.addUnlabelledSequenceFlow(checkTaskId, gatewayId);
        model.addUnlabelledSequenceFlow(gatewayId, firstPathAfterGateway);
        model.addUnlabelledSequenceFlow(gatewayId, secondPathAfterGateway);
        var sessionState = ImmutableSessionState.builder()
                .sessionId(aSessionId)
                .bpmnModel(model)
                .putNodeIdToModelInterfaceId(checkTaskId, "task1")
                .putNodeIdToModelInterfaceId(gatewayId, "gateway")
                .putNodeIdToModelInterfaceId(firstPathAfterGateway, "path1")
                .putNodeIdToModelInterfaceId(secondPathAfterGateway, "path2")
                .build();

        SequenceOfTasksDto callArguments = new SequenceOfTasksDto(
                aRetrospectiveSummary,
                "",
                "path1#path1",
                List.of(
                        "activity1",
                        "activity2"
                )
        );

        Result<FunctionCallResult, String> executorResult =
                executor.executeCall(mapper.writeValueAsString(callArguments), sessionState);
        assertTrue(executorResult.isOk(), "Result should be OK but is '%s'".formatted(executorResult.getError()));
        BpmnModel modelAfterModification = executorResult.getValue().updatedSessionState().bpmnModel();

        Optional<String> firstTaskId = modelAfterModification.findElementByName("activity1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = modelAfterModification.findElementByName("activity2");
        assertTrue(secondTaskId.isPresent());

        Set<String> predecessorTaskSuccessors = modelAfterModification.findSuccessors(firstPathAfterGateway);
        assertEquals(1, predecessorTaskSuccessors.size());
        assertTrue(predecessorTaskSuccessors.contains(firstTaskId.get()));

        Set<String> firstTaskSuccessors = modelAfterModification.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = modelAfterModification.findSuccessors(secondTaskId.get());

        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(0, secondTaskSuccessors.size());
        assertTrue(firstTaskSuccessors.contains(secondTaskId.get()));
    }

    @Test
    void works_correctly_for_sequence_with_single_element() throws JsonProcessingException {
        var model = new BpmnModel();
        String predecessorTaskId = model.addTask("task");
        String endEvent = model.addEndEvent();
        model.addUnlabelledSequenceFlow(predecessorTaskId, endEvent);
        var sessionState = ImmutableSessionState.builder()
                .sessionId(aSessionId)
                .bpmnModel(model)
                .putNodeIdToModelInterfaceId(predecessorTaskId, "task")
                .build();

        SequenceOfTasksDto callArguments = new SequenceOfTasksDto(
                aRetrospectiveSummary,
                "",
                "task#task",
                List.of(
                        "activity1"
                )
        );

        Result<FunctionCallResult, String> executorResult =
                executor.executeCall(mapper.writeValueAsString(callArguments), sessionState);
        assertTrue(executorResult.isOk(), "Result should be OK but is '%s'".formatted(executorResult.getError()));
        BpmnModel modelAfterModification = executorResult.getValue().updatedSessionState().bpmnModel();

        Optional<String> firstTaskId = modelAfterModification.findElementByName("activity1");
        assertTrue(firstTaskId.isPresent());

        Set<String> predecessorTaskSuccessors = modelAfterModification.findSuccessors(predecessorTaskId);
        assertEquals(1, predecessorTaskSuccessors.size());
        assertTrue(predecessorTaskSuccessors.contains(firstTaskId.get()));

        Set<String> firstTaskSuccessors = modelAfterModification.findSuccessors(firstTaskId.get());

        assertEquals(0, firstTaskSuccessors.size());
    }
}