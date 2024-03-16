package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.execution.AddXorGatewayExecutor;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.functions.parameter.XorGatewayDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddXorGatewayExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    RetrospectiveSummary aRetrospectiveSummary;
    SessionStateStore sessionStateStore;
    private AddXorGatewayExecutor executor;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        executor = new AddXorGatewayExecutor(new ToolCallArgumentsParser(mapper), sessionStateStore);
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void should_work_as_expected_for_existing_check_activity() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();
        String checkTaskId = model.addTask("task");
        XorGatewayDto callArguments = new XorGatewayDto(aRetrospectiveSummary, "", "elementName", "task", null, List.of("task1", "task2"));

        executor.executeCall(mapper.writeValueAsString(callArguments));

        Optional<String> firstTaskId = model.findElementByName("task1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = model.findElementByName("task2");
        assertTrue(secondTaskId.isPresent());

        Set<String> checkTaskSuccessors = model.findSuccessors(checkTaskId);
        assertEquals(1, checkTaskSuccessors.size());

        String openingGatewayId = checkTaskSuccessors.iterator().next();
        assertTrue(model.findSuccessors(openingGatewayId).contains(firstTaskId.get()));
        assertTrue(model.findSuccessors(openingGatewayId).contains(secondTaskId.get()));

        Set<String> openingGatewaySuccessors = model.findSuccessors(openingGatewayId);
        assertEquals(2, openingGatewaySuccessors.size());
        assertTrue(openingGatewaySuccessors.contains(firstTaskId.get()));
        assertTrue(openingGatewaySuccessors.contains(secondTaskId.get()));

        Set<String> firstTaskSuccessors = model.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = model.findSuccessors(firstTaskId.get());
        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(1, secondTaskSuccessors.size());
        assertEquals(firstTaskSuccessors, secondTaskSuccessors);
    }

    @Test
    void should_work_as_expected_for_new_check_activity_task() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();
        model.addTask("task");
        XorGatewayDto callArguments = new XorGatewayDto(aRetrospectiveSummary, "", "elementName", "checkActivity", "task", List.of("task1", "task2"));

        executor.executeCall(mapper.writeValueAsString(callArguments));

        Optional<String> checkTaskId = model.findElementByName("checkActivity");
        assertTrue(checkTaskId.isPresent());
        Optional<String> firstTaskId = model.findElementByName("task1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = model.findElementByName("task2");
        assertTrue(secondTaskId.isPresent());

        Set<String> checkTaskSuccessors = model.findSuccessors(checkTaskId.get());
        assertEquals(1, checkTaskSuccessors.size());

        String openingGatewayId = checkTaskSuccessors.iterator().next();
        assertTrue(model.findSuccessors(openingGatewayId).contains(firstTaskId.get()));
        assertTrue(model.findSuccessors(openingGatewayId).contains(secondTaskId.get()));

        Set<String> openingGatewaySuccessors = model.findSuccessors(openingGatewayId);
        assertEquals(2, openingGatewaySuccessors.size());
        assertTrue(openingGatewaySuccessors.contains(firstTaskId.get()));
        assertTrue(openingGatewaySuccessors.contains(secondTaskId.get()));

        Set<String> firstTaskSuccessors = model.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = model.findSuccessors(firstTaskId.get());
        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(1, secondTaskSuccessors.size());
        assertEquals(firstTaskSuccessors, secondTaskSuccessors);
    }

    @Test
    void should_work_as_expected_when_inserting_between_existing_tasks() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();
        String predecessorTaskId = model.addTask("predecessorTask");
        String successorTaskId = model.addTask("successorTask");
        model.addUnlabelledSequenceFlow(predecessorTaskId, successorTaskId);
        XorGatewayDto callArguments = new XorGatewayDto(aRetrospectiveSummary, "", "elementName", "checkActivity", "predecessorTask", List.of("task1", "task2"));

        executor.executeCall(mapper.writeValueAsString(callArguments));

        Optional<String> checkTaskId = model.findElementByName("checkActivity");
        assertTrue(checkTaskId.isPresent());
        Optional<String> firstTaskId = model.findElementByName("task1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = model.findElementByName("task2");
        assertTrue(secondTaskId.isPresent());

        Set<String> checkTaskSuccessors = model.findSuccessors(checkTaskId.get());
        assertEquals(1, checkTaskSuccessors.size());

        Set<String> predecessorTaskSuccessors = model.findSuccessors(predecessorTaskId);
        assertEquals(Set.of(checkTaskId.get()), predecessorTaskSuccessors);

        String openingGatewayId = checkTaskSuccessors.iterator().next();
        assertEquals(Set.of(firstTaskId.get(), secondTaskId.get()), model.findSuccessors(openingGatewayId));

        Set<String> openingGatewaySuccessors = model.findSuccessors(openingGatewayId);
        assertEquals(2, openingGatewaySuccessors.size());
        assertTrue(openingGatewaySuccessors.contains(firstTaskId.get()));
        assertTrue(openingGatewaySuccessors.contains(secondTaskId.get()));

        Set<String> firstTaskSuccessors = model.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = model.findSuccessors(firstTaskId.get());
        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(1, secondTaskSuccessors.size());
        assertEquals(firstTaskSuccessors, secondTaskSuccessors);

        String closingGatewayId = firstTaskSuccessors.iterator().next();
        Set<String> closingGatewaySuccessors = model.findSuccessors(closingGatewayId);
        assertEquals(1, closingGatewaySuccessors.size());
        assertTrue(closingGatewaySuccessors.contains(successorTaskId));
        System.out.println(model.asXmlString());
    }
}