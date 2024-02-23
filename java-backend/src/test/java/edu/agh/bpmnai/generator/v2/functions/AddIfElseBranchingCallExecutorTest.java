package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.IfElseBranchingDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddIfElseBranchingCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    AddIfElseBranchingCallExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new AddIfElseBranchingCallExecutor(new ToolCallArgumentsParser(mapper));
    }

    @Test
    void should_work_as_expected_for_existing_check_activity() {
        SessionState sessionState = new SessionState(List.of());
        BpmnModel model = sessionState.model();
        String checkTaskId = model.addTask("task");
        IfElseBranchingDto callArguments = new IfElseBranchingDto("", "task", null, "trueBranch", "falseBranch");
        JsonNode callArgumentsJson = mapper.valueToTree(callArguments);

        executor.executeCall(sessionState, "id", callArgumentsJson);

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
    void should_work_as_expected_for_new_check_activity_task() {
        SessionState sessionState = new SessionState(List.of());
        BpmnModel model = sessionState.model();
        model.addTask("task");

        IfElseBranchingDto callArguments = new IfElseBranchingDto("", "checkTask", "task", "trueBranch", "falseBranch");
        JsonNode callArgumentsJson = mapper.valueToTree(callArguments);

        executor.executeCall(sessionState, "id", callArgumentsJson);

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