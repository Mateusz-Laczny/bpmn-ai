package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.v2.functions.parameter.RetrospectiveSummary;
import edu.agh.bpmnai.generator.v2.functions.parameter.UserDescriptionReasoningDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsDescriptionDetailedEnoughCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private IsDescriptionDetailedEnoughCallExecutor executor;

    RetrospectiveSummary aRetrospectiveSummary;

    @BeforeEach
    void setUp() {
        executor = new IsDescriptionDetailedEnoughCallExecutor(new ToolCallArgumentsParser(mapper));
        aRetrospectiveSummary = new RetrospectiveSummary("");
    }

    @Test
    void result_contains_message_to_the_user_if_parameters_contain_message() throws JsonProcessingException {
        SessionState sessionState = new SessionState(List.of());
        String aMessage = "A message";
        UserDescriptionReasoningDto callArguments = new UserDescriptionReasoningDto(aRetrospectiveSummary, "background", aMessage, false);

        FunctionCallResult result = executor.executeCall(sessionState, "id", mapper.writeValueAsString(callArguments));
        assertTrue(result.successful());
        assertEquals(aMessage, result.messageToUser());
    }
}