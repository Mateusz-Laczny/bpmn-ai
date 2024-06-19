package edu.agh.bpmnai.generator.v2.session;

import edu.agh.bpmnai.generator.bpmn.BpmnToStringExporter;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.OpenAIChatCompletionApi;
import edu.agh.bpmnai.generator.openai.model.FunctionCallDto;
import edu.agh.bpmnai.generator.v2.*;
import edu.agh.bpmnai.generator.v2.functions.FinishAskingQuestionsFunction;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
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

class ModifyModelStateTest {

    OpenAI.OpenAIModel aModel = OpenAI.OpenAIModel.GPT_3_5_TURBO_16K;
    SessionStateStore sessionStateStore;
    ChatMessageBuilder chatMessageBuilder;
    String aSessionId = "ID";

    @BeforeEach
    void setUp() {
        sessionStateStore = new SessionStateStore();
        chatMessageBuilder = new ChatMessageBuilder();
    }

    @Test
    void returns_END_when_model_response_has_no_tool_calls() {
        var mockApi = mock(OpenAIChatCompletionApi.class);
        var state = new ModifyModelState(
                mock(FunctionExecutionService.class),
                mockApi,
                aModel,
                mock(ConversationHistoryStore.class),
                chatMessageBuilder,
                mock(BpmnToStringExporter.class)
        );
        when(mockApi.sendRequest(any(), anyList(), anySet(), any())).thenReturn(new ChatMessageDto(
                "aRole",
                "aContent",
                null,
                null
        ));
        var sessionState = ImmutableSessionState.builder()
                .sessionId(aSessionId)
                .sessionStatus(NEW)
                .model(new BpmnModel())
                .build();

        ImmutableSessionState sessionStateAfterProcessing = state.process(sessionState);
        assertEquals(PROMPTING_FINISHED_OK, sessionStateAfterProcessing.sessionStatus());
    }

    @Test
    void adds_error_response_to_conversation_when_called_function_does_not_exist() {
        var mockApi = mock(OpenAIChatCompletionApi.class);
        var mockFunctionExecutionService = mock(FunctionExecutionService.class);
        when(mockFunctionExecutionService.executeFunctionCall(any(), any())).thenReturn(Result.error(new CallError(
                NO_EXECUTOR_FOUND,
                "Error description"
        )));
        var state = new ModifyModelState(
                mockFunctionExecutionService,
                mockApi,
                aModel,
                mock(ConversationHistoryStore.class),
                chatMessageBuilder,
                mock(BpmnToStringExporter.class)
        );
        var callId = "id";
        var toolCall = new ToolCallDto(callId, "function", new FunctionCallDto("aName", ""));
        when(mockApi.sendRequest(any(), anyList(), anySet(), any())).thenReturn(new ChatMessageDto(
                "aRole",
                "aContent",
                null,
                List.of(toolCall)
        ));
        var sessionState = ImmutableSessionState.builder()
                .sessionId(aSessionId)
                .sessionStatus(NEW)
                .model(new BpmnModel())
                .build();

        ImmutableSessionState sessionStateAfterProcessing = state.process(sessionState);
        ChatMessageDto lastAddedMessage = sessionStateAfterProcessing.lastAddedMessage();
        assertEquals(MODIFY_MODEL, sessionStateAfterProcessing.sessionStatus());
        assertEquals(callId, lastAddedMessage.toolCallId());
    }

    @Test
    void adds_error_response_to_conversation_when_function_call_fails() {
        var mockApi = mock(OpenAIChatCompletionApi.class);
        var mockFunctionExecutionService = mock(FunctionExecutionService.class);
        when(mockFunctionExecutionService.executeFunctionCall(any(), any())).thenReturn(Result.error(new CallError(
                CALL_FAILED,
                "Error description"
        )));
        var state = new ModifyModelState(
                mockFunctionExecutionService,
                mockApi,
                aModel,
                mock(ConversationHistoryStore.class),
                chatMessageBuilder,
                mock(BpmnToStringExporter.class)
        );
        var callId = "id";
        var toolCall = new ToolCallDto(callId, "function", new FunctionCallDto("aName", ""));
        when(mockApi.sendRequest(any(), anyList(), anySet(), any())).thenReturn(new ChatMessageDto(
                "aRole",
                "aContent",
                null,
                List.of(toolCall)
        ));
        var sessionState = ImmutableSessionState.builder()
                .sessionId(aSessionId)
                .sessionStatus(NEW)
                .model(new BpmnModel())
                .build();

        ImmutableSessionState sessionStateAfterProcessing = state.process(sessionState);
        ChatMessageDto lastAddedMessage = sessionStateAfterProcessing.lastAddedMessage();
        assertEquals(MODIFY_MODEL, sessionStateAfterProcessing.sessionStatus());
        assertEquals(callId, lastAddedMessage.toolCallId());
    }

    @Test
    void stays_in_MODIFY_MODEL_state_if_function_call_is_successful() {
        var mockApi = mock(OpenAIChatCompletionApi.class);
        var mockFunctionExecutionService = mock(FunctionExecutionService.class);
        var state = new ModifyModelState(
                mockFunctionExecutionService,
                mockApi,
                aModel,
                mock(ConversationHistoryStore.class),
                chatMessageBuilder,
                mock(BpmnToStringExporter.class)
        );
        var callId = "id";
        var toolCall = new ToolCallDto(
                callId,
                "function",
                new FunctionCallDto(FinishAskingQuestionsFunction.FUNCTION_NAME, "")
        );
        when(mockApi.sendRequest(any(), anyList(), anySet(), any())).thenReturn(new ChatMessageDto(
                "aRole",
                "aContent",
                null,
                List.of(toolCall)
        ));
        var sessionState = ImmutableSessionState.builder()
                .sessionId(aSessionId)
                .sessionStatus(NEW)
                .model(new BpmnModel())
                .build();
        when(mockFunctionExecutionService.executeFunctionCall(
                any(),
                any()
        )).thenReturn(Result.ok(new FunctionCallResult(sessionState, "Some response")));

        ImmutableSessionState sessionStateAfterProcessing = state.process(sessionState);
        ChatMessageDto lastAddedMessage = sessionStateAfterProcessing.lastAddedMessage();
        assertEquals(MODIFY_MODEL, sessionStateAfterProcessing.sessionStatus());
        assertEquals(callId, lastAddedMessage.toolCallId());
    }
}