package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.NodeIdToModelInterfaceIdFunction;
import edu.agh.bpmnai.generator.v2.functions.execution.AddParallelGatewayCallExecutor;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
import edu.agh.bpmnai.generator.v2.functions.parameter.NullabilityCheck;
import edu.agh.bpmnai.generator.v2.functions.parameter.ParallelGatewayDto;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddParallelGatewayCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    AddParallelGatewayCallExecutor executor;

    RetrospectiveSummary aRetrospectiveSummary;

    SessionStateStore sessionStateStore;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();

        executor = new AddParallelGatewayCallExecutor(
                new ToolCallArgumentsParser(mapper, new NullabilityCheck()),
                sessionStateStore,
                new InsertElementIntoDiagram(),
                new NodeIdToModelInterfaceIdFunction(sessionStateStore)
        );
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void correctly_adds_a_gateway_with_two_tasks() throws JsonProcessingException {
        var model = new BpmnModel();
        String predecessorTaskId = model.addTask("task");
        sessionStateStore.setModelInterfaceId(predecessorTaskId, "task");
        sessionStateStore.setModel(model);
        ParallelGatewayDto callArguments = new ParallelGatewayDto(
                aRetrospectiveSummary,
                "",
                "elementName",
                new HumanReadableId("task", "task"),
                List.of(
                        new Activity("activity1", false),
                        new Activity("activity2", false)
                )
        );

        Result<String, String> executorResult = executor.executeCall(mapper.writeValueAsString(callArguments));
        assertTrue(executorResult.isOk(), "Result should be OK but is '%s'".formatted(executorResult.getError()));

        BpmnModel modelAfterModification = sessionStateStore.model();
        Optional<String> firstTaskId = modelAfterModification.findElementByName("activity1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = modelAfterModification.findElementByName("activity2");
        assertTrue(secondTaskId.isPresent());

        Set<String> predecessorTaskSuccessors = modelAfterModification.findSuccessors(predecessorTaskId);
        assertEquals(1, predecessorTaskSuccessors.size());
        String openingGatewayId = predecessorTaskSuccessors.iterator().next();
        Set<String> openingGatewaySuccessors = modelAfterModification.findSuccessors(openingGatewayId);
        assertTrue(openingGatewaySuccessors.contains(firstTaskId.get()));
        assertTrue(openingGatewaySuccessors.contains(secondTaskId.get()));

        Set<String> firstTaskSuccessors = modelAfterModification.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = modelAfterModification.findSuccessors(secondTaskId.get());

        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(1, secondTaskSuccessors.size());
        assertEquals(firstTaskSuccessors, secondTaskSuccessors);
    }

    @Test
    void removes_closing_gateway_if_it_has_a_single_predecessor() throws JsonProcessingException {
        var model = new BpmnModel();
        String predecessorTaskId = model.addTask("task");
        sessionStateStore.setModel(model);
        sessionStateStore.setModelInterfaceId(predecessorTaskId, "task");
        ParallelGatewayDto callArguments = new ParallelGatewayDto(
                aRetrospectiveSummary,
                "",
                "elementName",
                new HumanReadableId("task", "task"),
                List.of(
                        new Activity("activity1", false),
                        new Activity("activity2", true)
                )
        );

        Result<String, String> executorResult = executor.executeCall(mapper.writeValueAsString(callArguments));
        assertTrue(executorResult.isOk(), "Result should be OK but is '%s'".formatted(executorResult.getError()));

        BpmnModel modelAfterModification = sessionStateStore.model();
        Optional<String> firstTaskId = modelAfterModification.findElementByName("activity1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = modelAfterModification.findElementByName("activity2");
        assertTrue(secondTaskId.isPresent());

        Set<String> predecessorTaskSuccessors = modelAfterModification.findSuccessors(predecessorTaskId);
        assertEquals(1, predecessorTaskSuccessors.size());
        String openingGatewayId = predecessorTaskSuccessors.iterator().next();
        Set<String> openingGatewaySuccessors = modelAfterModification.findSuccessors(openingGatewayId);
        assertTrue(openingGatewaySuccessors.contains(firstTaskId.get()));
        assertTrue(openingGatewaySuccessors.contains(secondTaskId.get()));

        Set<String> firstTaskSuccessors = modelAfterModification.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = modelAfterModification.findSuccessors(secondTaskId.get());

        assertEquals(0, firstTaskSuccessors.size());
        assertEquals(1, secondTaskSuccessors.size());
    }
}