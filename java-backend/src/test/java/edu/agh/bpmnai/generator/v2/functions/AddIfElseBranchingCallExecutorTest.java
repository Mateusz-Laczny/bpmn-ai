package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.execution.ActivityService;
import edu.agh.bpmnai.generator.v2.functions.execution.AddIfElseBranchingCallExecutor;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
import edu.agh.bpmnai.generator.v2.functions.parameter.IfElseBranchingDto;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.v2.functions.parameter.DuplicateHandlingStrategy.ADD_NEW_INSTANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddIfElseBranchingCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    RetrospectiveSummary aRetrospectiveSummary;

    AddIfElseBranchingCallExecutor executor;

    SessionStateStore sessionStateStore;

    ActivityService activityService;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        activityService = new ActivityService();
        executor = new AddIfElseBranchingCallExecutor(
                new ToolCallArgumentsParser(mapper),
                sessionStateStore,
                activityService
        );
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void should_work_as_expected_for_existing_check_activity() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();
        String checkTaskId = model.addTask("task", "task");
        IfElseBranchingDto callArguments = new IfElseBranchingDto(
                aRetrospectiveSummary,
                "",
                "someName",
                "task",
                null,
                new Activity("trueBranch", ADD_NEW_INSTANCE, false),
                new Activity("falseBranch", ADD_NEW_INSTANCE, false)
        );

        var modelReference = new BpmnManagedReference(model);
        executor.executeCall(mapper.writeValueAsString(callArguments), modelReference);
        model = modelReference.getCurrentValue();

        Optional<String> trueBranchStartTaskId = model.findElementByModelFriendlyId("trueBranch");
        assertTrue(trueBranchStartTaskId.isPresent());
        Optional<String> falseBranchStartTaskId = model.findElementByModelFriendlyId("falseBranch");
        assertTrue(falseBranchStartTaskId.isPresent());

        Set<String> checkTaskSuccessors = model.findSuccessors(checkTaskId);
        assertEquals(1, checkTaskSuccessors.size());

        String gatewayId = checkTaskSuccessors.iterator().next();
        assertTrue(model.findSuccessors(gatewayId).contains(trueBranchStartTaskId.get()));
        assertTrue(model.findSuccessors(gatewayId).contains(falseBranchStartTaskId.get()));
    }

    @Test
    void should_work_as_expected_for_new_check_activity_task() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();
        model.addTask("task", "task");

        IfElseBranchingDto callArguments = new IfElseBranchingDto(
                aRetrospectiveSummary,
                "",
                "someName",
                "checkActivity",
                "task",
                new Activity("trueBranch", ADD_NEW_INSTANCE, false),
                new Activity("falseBranch", ADD_NEW_INSTANCE, false)
        );

        var modelReference = new BpmnManagedReference(model);
        executor.executeCall(mapper.writeValueAsString(callArguments), modelReference);
        model = modelReference.getCurrentValue();

        Optional<String> checkTaskId = model.findElementByModelFriendlyId("checkActivity");
        assertTrue(checkTaskId.isPresent());
        Optional<String> trueBranchStartTaskId = model.findElementByModelFriendlyId("trueBranch");
        assertTrue(trueBranchStartTaskId.isPresent());
        Optional<String> falseBranchStartTaskId = model.findElementByModelFriendlyId("falseBranch");
        assertTrue(falseBranchStartTaskId.isPresent());

        Set<String> checkTaskSuccessors = model.findSuccessors(checkTaskId.get());
        assertEquals(1, checkTaskSuccessors.size());

        String gatewayId = checkTaskSuccessors.iterator().next();
        assertTrue(model.findSuccessors(gatewayId).contains(trueBranchStartTaskId.get()));
        assertTrue(model.findSuccessors(gatewayId).contains(falseBranchStartTaskId.get()));
    }

    @Test
    void should_work_for_start_activity_as_predecessor() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();

        IfElseBranchingDto callArguments = new IfElseBranchingDto(
                aRetrospectiveSummary,
                "",
                "someName",
                "checkActivity",
                "Start",
                new Activity("trueBranch", ADD_NEW_INSTANCE, false),
                new Activity("falseBranch", ADD_NEW_INSTANCE, false)
        );

        var modelReference = new BpmnManagedReference(model);
        executor.executeCall(mapper.writeValueAsString(callArguments), modelReference);
        model = modelReference.getCurrentValue();

        Optional<String> checkTaskId = model.findElementByModelFriendlyId("checkActivity");
        assertTrue(checkTaskId.isPresent());
        Optional<String> trueBranchStartTaskId = model.findElementByModelFriendlyId("trueBranch");
        assertTrue(trueBranchStartTaskId.isPresent());
        Optional<String> falseBranchStartTaskId = model.findElementByModelFriendlyId("falseBranch");
        assertTrue(falseBranchStartTaskId.isPresent());

        Set<String> checkTaskSuccessors = model.findSuccessors(checkTaskId.get());
        assertEquals(1, checkTaskSuccessors.size());

        Set<String> startEventSuccessors = model.findSuccessors(model.getStartEvent());
        assertEquals(1, startEventSuccessors.size());

        String gatewayId = checkTaskSuccessors.iterator().next();
        assertTrue(model.findSuccessors(gatewayId).contains(trueBranchStartTaskId.get()));
        assertTrue(model.findSuccessors(gatewayId).contains(falseBranchStartTaskId.get()));
    }
}