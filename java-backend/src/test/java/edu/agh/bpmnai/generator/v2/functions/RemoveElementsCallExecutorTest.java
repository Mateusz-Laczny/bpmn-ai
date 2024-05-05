package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.v2.functions.execution.RemoveElementsCallExecutor;
import edu.agh.bpmnai.generator.v2.functions.parameter.NullabilityCheck;
import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveElementsFunctionCallDto;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoveElementsCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    SessionStateStore sessionStateStore;

    RetrospectiveSummary aRetrospectiveSummary;
    private RemoveElementsCallExecutor executor;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        executor = new RemoveElementsCallExecutor(
                new ToolCallArgumentsParser(mapper, new NullabilityCheck()),
                sessionStateStore
        );
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void removes_task_from_the_model() throws JsonProcessingException {
        BpmnModel model = sessionStateStore.model();
        String taskId = model.addTask("task");
        RemoveElementsFunctionCallDto callArguments = new RemoveElementsFunctionCallDto(aRetrospectiveSummary, "",
                                                                                        List.of(new HumanReadableId(
                                                                                                "task",
                                                                                                taskId
                                                                                        ))
        );

        var modelReference = new BpmnManagedReference(model);
        executor.executeCall(mapper.writeValueAsString(callArguments), modelReference);
        model = modelReference.getCurrentValue();

        assertTrue(model.findElementByName("task").isEmpty());
    }
}