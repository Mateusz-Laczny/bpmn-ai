package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.v2.NodeIdToModelInterfaceIdFunction;
import edu.agh.bpmnai.generator.v2.functions.execution.AddXorGatewayCallExecutor;
import edu.agh.bpmnai.generator.v2.functions.parameter.NullabilityCheck;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.functions.parameter.Task;
import edu.agh.bpmnai.generator.v2.functions.parameter.XorGatewayDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddXorGatewayCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    RetrospectiveSummary aRetrospectiveSummary;
    SessionStateStore sessionStateStore;
    AddXorGatewayCallExecutor executor;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        executor = new AddXorGatewayCallExecutor(
                new ToolCallArgumentsParser(mapper, new NullabilityCheck()),
                sessionStateStore,
                new InsertElementIntoDiagram(),
                new NodeIdToModelInterfaceIdFunction(sessionStateStore)
        );
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void should_work_as_expected_for_existing_check_activity() throws JsonProcessingException {
        var model = new BpmnModel();
        String checkTaskId = model.addTask("task");
        sessionStateStore.setModel(model);
        sessionStateStore.setModelInterfaceId(checkTaskId, "task");
        XorGatewayDto callArguments = new XorGatewayDto(
                aRetrospectiveSummary,
                "",
                "elementName",
                new HumanReadableId("task", "task").asString(),
                null,
                List.of(new Task("task1", false), new Task("task2", false))
        );


        executor.executeCall(mapper.writeValueAsString(callArguments));
        BpmnModel modelAfterModification = sessionStateStore.model();

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
        Set<String> secondTaskSuccessors = modelAfterModification.findSuccessors(firstTaskId.get());
        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(1, secondTaskSuccessors.size());
        assertEquals(firstTaskSuccessors, secondTaskSuccessors);
    }

    @Test
    void should_work_as_expected_for_new_check_activity_task() throws JsonProcessingException {
        var model = new BpmnModel();
        String taskId = model.addTask("task");
        sessionStateStore.setModel(model);
        sessionStateStore.setModelInterfaceId(taskId, "task");
        XorGatewayDto callArguments = new XorGatewayDto(
                aRetrospectiveSummary,
                "",
                "elementName",
                "checkTask",
                new HumanReadableId("task", taskId),
                List.of(new Task("task1", false), new Task("task2", false))
        );


        executor.executeCall(mapper.writeValueAsString(callArguments));
        BpmnModel modelAfterModification = sessionStateStore.model();

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
        sessionStateStore.setModelInterfaceId(predecessorTaskId, "predecessorTask");
        String successorTaskId = model.addTask("successorTask");
        sessionStateStore.setModelInterfaceId(predecessorTaskId, "successorTask");
        model.addUnlabelledSequenceFlow(predecessorTaskId, successorTaskId);
        sessionStateStore.setModel(model);

        XorGatewayDto callArguments = new XorGatewayDto(
                aRetrospectiveSummary,
                "",
                "elementName",
                "checkTask",
                new HumanReadableId("predecessorTask", predecessorTaskId),
                List.of(new Task("task1", false), new Task("task2", false))
        );


        executor.executeCall(mapper.writeValueAsString(callArguments));
        BpmnModel modelAfterModification = sessionStateStore.model();

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