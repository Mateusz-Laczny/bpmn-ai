package edu.agh.bpmnai.generator.v2.session;

import edu.agh.bpmnai.generator.bpmn.BpmnToStringExporter;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.OpenAIChatCompletionApi;
import edu.agh.bpmnai.generator.openai.model.FunctionCallDto;
import edu.agh.bpmnai.generator.v2.*;
import edu.agh.bpmnai.generator.v2.functions.AskQuestionFunction;
import edu.agh.bpmnai.generator.v2.functions.FinishAskingQuestionsFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.agh.bpmnai.generator.v2.CallErrorType.CALL_FAILED;
import static edu.agh.bpmnai.generator.v2.CallErrorType.NO_EXECUTOR_FOUND;
import static edu.agh.bpmnai.generator.v2.session.SessionStatus.*;
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
        var state = new AskQuestionsState(mock(FunctionExecutionService.class), mockApi, aModel, sessionStateStore, chatMessageBuilder, mock(BpmnToStringExporter.class));
        when(mockApi.sendRequest(any(), anyList(), anySet(), any())).thenReturn(new ChatMessageDto("aRole", "aContent", null, null, null));

        SessionStatus status = state.process("aUserRequest");

        assertEquals(ASK_QUESTIONS, status);
    }

    @Test
    void adds_error_response_to_conversation_when_called_function_does_not_exist() {
        var mockApi = mock(OpenAIChatCompletionApi.class);
        var mockFunctionExecutionService = mock(FunctionExecutionService.class);
        when(mockFunctionExecutionService.executeFunctionCall(any())).thenReturn(Result.error(new CallError(NO_EXECUTOR_FOUND, "Some error description")));
        var state = new AskQuestionsState(mockFunctionExecutionService, mockApi, aModel, sessionStateStore, chatMessageBuilder, mock(BpmnToStringExporter.class));
        var callId = "id";
        var toolCall = new ToolCallDto(callId, "function", new FunctionCallDto("aName", ""));
        when(mockApi.sendRequest(any(), anyList(), anySet(), any())).thenReturn(new ChatMessageDto("aRole", "aContent", null, List.of(toolCall), null));

        SessionStatus status = state.process("aUserRequest");

        ChatMessageDto lastAddedMessage = sessionStateStore.lastAddedMessage();
        assertEquals(ASK_QUESTIONS, status);
        assertEquals(callId, lastAddedMessage.toolCallId());
    }

    @Test
    void adds_error_response_to_conversation_when_function_call_fails() {
        var mockApi = mock(OpenAIChatCompletionApi.class);
        var mockFunctionExecutionService = mock(FunctionExecutionService.class);
        when(mockFunctionExecutionService.executeFunctionCall(any())).thenReturn(Result.error(new CallError(CALL_FAILED, "Some error description")));
        var state = new AskQuestionsState(mockFunctionExecutionService, mockApi, aModel, sessionStateStore, chatMessageBuilder, mock(BpmnToStringExporter.class));
        var callId = "id";
        var toolCall = new ToolCallDto(callId, "function", new FunctionCallDto("aName", ""));
        when(mockApi.sendRequest(any(), anyList(), anySet(), any())).thenReturn(new ChatMessageDto("aRole", "aContent", null, List.of(toolCall), null));

        SessionStatus status = state.process("aUserRequest");

        ChatMessageDto lastAddedMessage = sessionStateStore.lastAddedMessage();
        assertEquals(ASK_QUESTIONS, status);
        assertEquals(callId, lastAddedMessage.toolCallId());
    }

    @Test
    void goes_to_next_step_if_finish_asking_questions_is_called() {
        var mockApi = mock(OpenAIChatCompletionApi.class);
        var mockFunctionExecutionService = mock(FunctionExecutionService.class);
        when(mockFunctionExecutionService.executeFunctionCall(any())).thenReturn(Result.ok(""));
        var state = new AskQuestionsState(mockFunctionExecutionService, mockApi, aModel, sessionStateStore, chatMessageBuilder, mock(BpmnToStringExporter.class));
        var callId = "id";
        var toolCall = new ToolCallDto(callId, "function", new FunctionCallDto(FinishAskingQuestionsFunction.FUNCTION_NAME, ""));
        when(mockApi.sendRequest(any(), anyList(), anySet(), any())).thenReturn(new ChatMessageDto("aRole", "aContent", null, List.of(toolCall), null));

        SessionStatus status = state.process("aUserRequest");

        ChatMessageDto lastAddedMessage = sessionStateStore.lastAddedMessage();
        assertEquals(REASON_ABOUT_TASKS_AND_PROCESS_FLOW, status);
        assertEquals(callId, lastAddedMessage.toolCallId());
    }

    @Test
    void returns_end_and_adds_question_to_conversation_thread_if_ask_question_is_called() {
        var mockApi = mock(OpenAIChatCompletionApi.class);
        var mockFunctionExecutionService = mock(FunctionExecutionService.class);
        String question = "aQuestion";
        when(mockFunctionExecutionService.executeFunctionCall(any())).thenReturn(Result.ok(question));
        var state = new AskQuestionsState(mockFunctionExecutionService, mockApi, aModel, sessionStateStore, chatMessageBuilder, mock(BpmnToStringExporter.class));
        var callId = "id";
        var toolCall = new ToolCallDto(callId, "function", new FunctionCallDto(AskQuestionFunction.FUNCTION_NAME, ""));
        when(mockApi.sendRequest(any(), anyList(), anySet(), any())).thenReturn(new ChatMessageDto("aRole", "aContent", null, List.of(toolCall), null));

        SessionStatus status = state.process("aUserRequest");

        ChatMessageDto lastAddedMessage = sessionStateStore.lastAddedMessage();
        assertEquals(END, status);
        assertEquals(callId, lastAddedMessage.toolCallId());

        List<ChatMessageDto> allMessages = sessionStateStore.messages();
        ChatMessageDto secondLastAddedMessage = allMessages.get(allMessages.size() - 2);
        assertEquals(question, secondLastAddedMessage.userFacingContent());
    }
}