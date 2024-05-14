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
                new ToolCallArgumentsParser(mapper, new NullabilityCheck()),
                sessionStateStore,
                new InsertElementIntoDiagram(),
                new NodeIdToModelInterfaceIdFunction(sessionStateStore)
        );
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void works_as_expected() throws JsonProcessingException {
        var model = new BpmnModel();
        String predecessorTaskId = model.addTask("task");
        sessionStateStore.setModelInterfaceId(predecessorTaskId, "task");
        sessionStateStore.setModel(model);

        SequenceOfTasksDto callArguments = new SequenceOfTasksDto(
                aRetrospectiveSummary,
                "",
                "task#task",
                List.of(
                        "activity1",
                        "activity2"
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
        sessionStateStore.setModelInterfaceId(checkTaskId, "task");
        String gatewayId = model.addGateway(EXCLUSIVE, "gateway");
        sessionStateStore.setModelInterfaceId(gatewayId, "gateway");
        String firstPathAfterGateway = model.addTask("path1");
        sessionStateStore.setModelInterfaceId(firstPathAfterGateway, "path1");
        String secondPathAfterGateway = model.addTask("path2");
        sessionStateStore.setModelInterfaceId(secondPathAfterGateway, "path2");
        model.addUnlabelledSequenceFlow(model.getStartEvent(), checkTaskId);
        model.addUnlabelledSequenceFlow(checkTaskId, gatewayId);
        model.addUnlabelledSequenceFlow(gatewayId, firstPathAfterGateway);
        model.addUnlabelledSequenceFlow(gatewayId, secondPathAfterGateway);
        sessionStateStore.setModel(model);

        SequenceOfTasksDto callArguments = new SequenceOfTasksDto(
                aRetrospectiveSummary,
                "",
                "path1#path1",
                List.of(
                        "activity1",
                        "activity2"
                )
        );

        Result<String, String> executorResult = executor.executeCall(mapper.writeValueAsString(callArguments));
        assertTrue(executorResult.isOk(), "Result should be OK but is '%s'".formatted(executorResult.getError()));
        BpmnModel modelAfterModification = sessionStateStore.model();

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
}