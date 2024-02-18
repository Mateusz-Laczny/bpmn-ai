package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import edu.agh.bpmnai.generator.v2.FunctionCallResponseDto;
import edu.agh.bpmnai.generator.v2.UserDescriptionReasoningDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IsDescriptionDetailedEnoughCallExecutorTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void works_as_expected() throws JsonProcessingException {
        SessionState sessionState = new SessionState(List.of());
        UserDescriptionReasoningDto callArguments = new UserDescriptionReasoningDto("background", List.of(), "Message content");
        JsonNode callArgumentsJson = mapper.valueToTree(callArguments);
        var executor = new IsDescriptionDetailedEnoughCallExecutor(mapper);

        FunctionCallResult result = executor.executeCall(sessionState, "id", callArgumentsJson);
        sessionState.appendMessage(result.response().get());
        sessionState.appendUserMessage("Response");

        String responseContent = mapper.writeValueAsString(new FunctionCallResponseDto(true, Map.of("response", "Response")));
        List<ChatMessageDto> messages = sessionState.messages();
        ChatMessageDto modifiedUserResponse = messages.get(messages.size() - 2);
        assertEquals(responseContent, modifiedUserResponse.content());
    }
}