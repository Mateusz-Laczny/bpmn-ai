package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.execution.AddIfElseBranchingCallExecutor;
import edu.agh.bpmnai.generator.v2.functions.parameter.IfElseBranchingDto;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddIfElseBranchingCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    RetrospectiveSummary aRetrospectiveSummary;

    AddIfElseBranchingCallExecutor executor;

    SessionStateStore sessionStateStore;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        executor = new AddIfElseBranchingCallExecutor(new ToolCallArgumentsParser(mapper), sessionStateStore);
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void should_work_as_expected_for_existing_check_activity() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();
        String checkTaskId = model.addTask("task");
        IfElseBranchingDto callArguments = new IfElseBranchingDto(aRetrospectiveSummary, "", "task", null, "trueBranch", "falseBranch");

        executor.executeCall(mapper.writeValueAsString(callArguments));

        Optional<String> trueBranchStartTaskId = model.findTaskIdByName("trueBranch");
        assertTrue(trueBranchStartTaskId.isPresent());
        Optional<String> falseBranchStartTaskId = model.findTaskIdByName("falseBranch");
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
        model.addTask("task");

        IfElseBranchingDto callArguments = new IfElseBranchingDto(aRetrospectiveSummary, "", "checkTask", "task", "trueBranch", "falseBranch");

        executor.executeCall(mapper.writeValueAsString(callArguments));

        Optional<String> checkTaskId = model.findTaskIdByName("checkTask");
        assertTrue(checkTaskId.isPresent());
        Optional<String> trueBranchStartTaskId = model.findTaskIdByName("trueBranch");
        assertTrue(trueBranchStartTaskId.isPresent());
        Optional<String> falseBranchStartTaskId = model.findTaskIdByName("falseBranch");
        assertTrue(falseBranchStartTaskId.isPresent());

        Set<String> checkTaskSuccessors = model.findSuccessors(checkTaskId.get());
        assertEquals(1, checkTaskSuccessors.size());

        String gatewayId = checkTaskSuccessors.iterator().next();
        assertTrue(model.findSuccessors(gatewayId).contains(trueBranchStartTaskId.get()));
        assertTrue(model.findSuccessors(gatewayId).contains(falseBranchStartTaskId.get()));
    }
}