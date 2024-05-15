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
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddWhileLoopCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    RetrospectiveSummary aRetrospectiveSummary;
    SessionStateStore sessionStateStore;
    AddWhileLoopCallExecutor executor;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        executor = new AddWhileLoopCallExecutor(
                new ToolCallArgumentsParser(mapper, new NullabilityCheck()),
                sessionStateStore,
                new InsertElementIntoDiagram(new CheckIfValidInsertionPoint(sessionStateStore)),
                new NodeIdToModelInterfaceIdFunction(sessionStateStore),
                new FindInsertionPointForSubprocessWithCheckTask(
                        sessionStateStore,
                        new CheckIfValidInsertionPoint(sessionStateStore)
                )
        );
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void should_work_as_expected_for_existing_check_activity() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();
        String checkTaskId = model.addTask("task");
        sessionStateStore.setModelInterfaceId(checkTaskId, "task");
        sessionStateStore.setModel(model);
        WhileLoopDto callArguments = new WhileLoopDto(
                aRetrospectiveSummary,
                "someName",
                new HumanReadableId("task", "task").asString(),
                null,
                List.of("task1", "task2")
        );


        Result<String, String> executorResult = executor.executeCall(mapper.writeValueAsString(callArguments));
        assertTrue(executorResult.isOk(), "Result should be OK but is '%s'".formatted(executorResult.getError()));
        BpmnModel modelAfterModification = sessionStateStore.model();

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
        BpmnModel model = sessionStateStore.model();
        String predecessorTaskId = model.addTask("task");
        sessionStateStore.setModelInterfaceId(predecessorTaskId, "task");
        sessionStateStore.setModel(model);

        WhileLoopDto callArguments = new WhileLoopDto(
                aRetrospectiveSummary,
                "someName",
                "checkTask",
                "taks#task",
                List.of("task1", "task2")
        );

        Result<String, String> executorResult = executor.executeCall(mapper.writeValueAsString(callArguments));
        assertTrue(executorResult.isOk(), "Result should be OK but is '%s'".formatted(executorResult.getError()));
        BpmnModel modelAfterModification = sessionStateStore.model();

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