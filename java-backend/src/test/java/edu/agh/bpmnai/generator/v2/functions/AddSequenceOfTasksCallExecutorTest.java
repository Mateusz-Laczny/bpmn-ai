package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.execution.AddSequenceOfTasksCallExecutor;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.functions.parameter.SequenceOfTasksDto;
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

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        executor = new AddSequenceOfTasksCallExecutor(
                new ToolCallArgumentsParser(mapper),
                sessionStateStore,
                new InsertElementIntoDiagram()
        );
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void works_as_expected() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();
        String predecessorTaskId = model.addTask("task", "task");
        SequenceOfTasksDto callArguments = new SequenceOfTasksDto(
                aRetrospectiveSummary,
                "",
                "task",
                List.of(
                        new Activity("activity1", false),
                        new Activity("activity2", false)
                )
        );

        var modelReference = new BpmnManagedReference(model);
        executor.executeCall(mapper.writeValueAsString(callArguments), modelReference);
        model = modelReference.getCurrentValue();

        Optional<String> firstTaskId = model.findElementByModelFriendlyId("activity1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = model.findElementByModelFriendlyId("activity2");
        assertTrue(secondTaskId.isPresent());

        Set<String> predecessorTaskSuccessors = model.findSuccessors(predecessorTaskId);
        assertEquals(1, predecessorTaskSuccessors.size());
        assertTrue(predecessorTaskSuccessors.contains(firstTaskId.get()));

        Set<String> firstTaskSuccessors = model.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = model.findSuccessors(secondTaskId.get());

        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(0, secondTaskSuccessors.size());
        assertTrue(firstTaskSuccessors.contains(secondTaskId.get()));
    }

    @Test
    void works_as_expected_when_inserting_the_sequence_into_an_existing_model() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();
        String checkTaskId = model.addTask("task", "task");
        String gatewayId = model.addGateway(EXCLUSIVE, "gateway");
        String firstPathAfterGateway = model.addTask("path1", "path1");
        String secondPathAfterGateway = model.addTask("path2", "path2");
        model.addUnlabelledSequenceFlow(model.getStartEvent(), checkTaskId);
        model.addUnlabelledSequenceFlow(checkTaskId, gatewayId);
        model.addUnlabelledSequenceFlow(gatewayId, firstPathAfterGateway);
        model.addUnlabelledSequenceFlow(gatewayId, secondPathAfterGateway);
        SequenceOfTasksDto callArguments = new SequenceOfTasksDto(
                aRetrospectiveSummary,
                "",
                "path1",
                List.of(
                        new Activity("activity1", false),
                        new Activity("activity2", false)
                )
        );

        var modelReference = new BpmnManagedReference(model);
        executor.executeCall(mapper.writeValueAsString(callArguments), modelReference);
        model = modelReference.getCurrentValue();

        Optional<String> firstTaskId = model.findElementByModelFriendlyId("activity1");
        assertTrue(firstTaskId.isPresent());
        Optional<String> secondTaskId = model.findElementByModelFriendlyId("activity2");
        assertTrue(secondTaskId.isPresent());

        Set<String> predecessorTaskSuccessors = model.findSuccessors(firstPathAfterGateway);
        assertEquals(1, predecessorTaskSuccessors.size());
        assertTrue(predecessorTaskSuccessors.contains(firstTaskId.get()));

        Set<String> firstTaskSuccessors = model.findSuccessors(firstTaskId.get());
        Set<String> secondTaskSuccessors = model.findSuccessors(secondTaskId.get());

        assertEquals(1, firstTaskSuccessors.size());
        assertEquals(0, secondTaskSuccessors.size());
        assertTrue(firstTaskSuccessors.contains(secondTaskId.get()));
    }
}