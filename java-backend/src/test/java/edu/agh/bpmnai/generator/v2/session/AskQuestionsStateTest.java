package edu.agh.bpmnai.generator.v2.session;

import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.OpenAIChatCompletionApi;
import edu.agh.bpmnai.generator.openai.model.FunctionCallDto;
import edu.agh.bpmnai.generator.v2.ChatMessageBuilder;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import edu.agh.bpmnai.generator.v2.FunctionExecutionService;
import edu.agh.bpmnai.generator.v2.ToolCallDto;
import edu.agh.bpmnai.generator.v2.functions.AskQuestionFunction;
import edu.agh.bpmnai.generator.v2.functions.FinishAskingQuestionsFunction;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.ASK_QUESTIONS;
import static edu.agh.bpmnai.generator.v2.session.SessionStatus.REASON_ABOUT_TASKS_AND_PROCESS_FLOW;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AskQuestionsStateTest {

    OpenAI.OpenAIModel aModel = OpenAI.OpenAIModel.GPT_3_5_TURBO_16K;

    SessionStateStore sessionStateStore;

    ChatMessageBuilder chatMessageBuilder;

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        chatMessageBuilder = new ChatMessageBuilder();
    }

    @Test
    void returns_ASK_QUESTIONS_when_model_response_has_no_tool_calls() {
        var mockApi = mock(OpenAIChatCompletionApi.class);
        var state = new AskQuestionsState(mock(FunctionExecutionService.class), mockApi, aModel, sessionStateStore, chatMessageBuilder);
        when(mockApi.sendRequest(any(), anyList(), anySet(), any())).thenReturn(new ChatMessageDto("aRole", "aContent", null));

        SessionStatus status = state.process("aUserRequest");

        assertEquals(ASK_QUESTIONS, status);
    }

    @Test
    void adds_error_response_to_conversation_when_called_function_does_not_exist() {
        var mockApi = mock(OpenAIChatCompletionApi.class);
        var mockFunctionExecutionService = mock(FunctionExecutionService.class);
        when(mockFunctionExecutionService.executeFunctionCall(any())).thenReturn(Optional.empty());
        var state = new AskQuestionsState(mockFunctionExecutionService, mockApi, aModel, sessionStateStore, chatMessageBuilder);
        var callId = "id";
        var toolCall = new ToolCallDto(callId, "function", new FunctionCallDto("aName", ""));
        when(mockApi.sendRequest(any(), anyList(), anySet(), any())).thenReturn(new ChatMessageDto("aRole", "aContent", null, List.of(toolCall)));

        SessionStatus status = state.process("aUserRequest");

        ChatMessageDto lastAddedMessage = sessionStateStore.lastAddedMessage();
        assertEquals(ASK_QUESTIONS, status);
        assertEquals(callId, lastAddedMessage.toolCallId());
    }

    @Test
    void adds_error_response_to_conversation_when_function_call_fails() {
        var mockApi = mock(OpenAIChatCompletionApi.class);
        var mockFunctionExecutionService = mock(FunctionExecutionService.class);
        when(mockFunctionExecutionService.executeFunctionCall(any())).thenReturn(Optional.of(new FunctionCallResult(List.of("error"), emptyMap(), null)));
        var state = new AskQuestionsState(mockFunctionExecutionService, mockApi, aModel, sessionStateStore, chatMessageBuilder);
        var callId = "id";
        var toolCall = new ToolCallDto(callId, "function", new FunctionCallDto("aName", ""));
        when(mockApi.sendRequest(any(), anyList(), anySet(), any())).thenReturn(new ChatMessageDto("aRole", "aContent", null, List.of(toolCall)));

        SessionStatus status = state.process("aUserRequest");

        ChatMessageDto lastAddedMessage = sessionStateStore.lastAddedMessage();
        assertEquals(ASK_QUESTIONS, status);
        assertEquals(callId, lastAddedMessage.toolCallId());
    }

    @Test
    void goes_to_next_step_if_finish_asking_questions_is_called() {
        var mockApi = mock(OpenAIChatCompletionApi.class);
        var mockFunctionExecutionService = mock(FunctionExecutionService.class);
        when(mockFunctionExecutionService.executeFunctionCall(any())).thenReturn(Optional.of(FunctionCallResult.successfulCall()));
        var state = new AskQuestionsState(mockFunctionExecutionService, mockApi, aModel, sessionStateStore, chatMessageBuilder);
        var callId = "id";
        var toolCall = new ToolCallDto(callId, "function", new FunctionCallDto(FinishAskingQuestionsFunction.FUNCTION_NAME, ""));
        when(mockApi.sendRequest(any(), anyList(), anySet(), any())).thenReturn(new ChatMessageDto("aRole", "aContent", null, List.of(toolCall)));

        SessionStatus status = state.process("aUserRequest");

        ChatMessageDto lastAddedMessage = sessionStateStore.lastAddedMessage();
        assertEquals(REASON_ABOUT_TASKS_AND_PROCESS_FLOW, status);
        assertEquals(callId, lastAddedMessage.toolCallId());
    }

    @Test
    void remains_in_ASK_QUESTIONS_state_if_ask_questions_is_called() {
        var mockApi = mock(OpenAIChatCompletionApi.class);
        var mockFunctionExecutionService = mock(FunctionExecutionService.class);
        when(mockFunctionExecutionService.executeFunctionCall(any())).thenReturn(Optional.of(FunctionCallResult.successfulCall()));
        var state = new AskQuestionsState(mockFunctionExecutionService, mockApi, aModel, sessionStateStore, chatMessageBuilder);
        var callId = "id";
        var toolCall = new ToolCallDto(callId, "function", new FunctionCallDto(AskQuestionFunction.FUNCTION_NAME, ""));
        when(mockApi.sendRequest(any(), anyList(), anySet(), any())).thenReturn(new ChatMessageDto("aRole", "aContent", null, List.of(toolCall)));

        SessionStatus status = state.process("aUserRequest");

        ChatMessageDto lastAddedMessage = sessionStateStore.lastAddedMessage();
        assertEquals(ASK_QUESTIONS, status);
        assertEquals(callId, lastAddedMessage.toolCallId());
    }
}