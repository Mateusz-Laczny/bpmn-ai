package edu.agh.bpmnai.generator.v2.session;

import edu.agh.bpmnai.generator.bpmn.BpmnToStringExporter;
import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.OpenAIChatCompletionApi;
import edu.agh.bpmnai.generator.v2.*;
import edu.agh.bpmnai.generator.v2.functions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.*;

@Service
@Slf4j
public class AskQuestionsState {
    private static final Set<ChatFunctionDto> AVAILABLE_FUNCTIONS_IN_THIS_STATE = Set.of(
            AskQuestionFunction.FUNCTION_DTO,
            RespondWithoutModifyingDiagramFunction.FUNCTION_DTO,
            FinishAskingQuestionsFunction.FUNCTION_DTO
    );

    private final FunctionExecutionService functionExecutionService;

    private final OpenAIChatCompletionApi chatCompletionApi;

    private final OpenAI.OpenAIModel usedModel;

    private final SessionStateStore sessionStateStore;

    private final ChatMessageBuilder chatMessageBuilder;

    private final BpmnToStringExporter bpmnToStringExporter;

    @Autowired
    public AskQuestionsState(FunctionExecutionService functionExecutionService, OpenAIChatCompletionApi chatCompletionApi, OpenAI.OpenAIModel usedModel, SessionStateStore sessionStateStore, ChatMessageBuilder chatMessageBuilder, BpmnToStringExporter bpmnToStringExporter) {
        this.functionExecutionService = functionExecutionService;
        this.chatCompletionApi = chatCompletionApi;
        this.usedModel = usedModel;
        this.sessionStateStore = sessionStateStore;
        this.chatMessageBuilder = chatMessageBuilder;
        this.bpmnToStringExporter = bpmnToStringExporter;
    }

    public SessionStatus process(String userRequestContent) {
        userRequestContent =
                "BEGIN USER REQUEST\n"
                + userRequestContent
                + "END USER REQUEST\n"
                + "\n"
                + "BEGIN REQUEST CONTEXT\n"
                + "Current diagram state:\n" + bpmnToStringExporter.export(sessionStateStore.model())
                + "\n"
                + "END REQUEST CONTEXT";
        log.info("Request text sent to LLM: '{}'", userRequestContent);

        if (sessionStateStore.numberOfMessages() > 0) {
            ChatMessageDto lastMessage = sessionStateStore.lastAddedMessage();
            if (lastMessage.hasToolCalls()) {
                ToolCallDto toolCall = lastMessage.toolCalls().get(0);
                var response = chatMessageBuilder.buildToolCallResponseMessage(toolCall.id(), new FunctionCallResponseDto(true, Map.of("response", userRequestContent)));
                sessionStateStore.appendMessage(response);
            } else {
                sessionStateStore.appendMessage(chatMessageBuilder.buildUserMessage(userRequestContent));
            }
        } else {
            sessionStateStore.appendMessage(chatMessageBuilder.buildUserMessage(userRequestContent));
        }

        ChatMessageDto chatResponse = chatCompletionApi.sendRequest(usedModel, sessionStateStore.messages(), AVAILABLE_FUNCTIONS_IN_THIS_STATE, "auto");

        if (chatResponse.toolCalls() == null) {
            log.warn("No tools calls even though a tool call was expected");
            sessionStateStore.appendMessage(chatResponse);
            sessionStateStore.appendMessage(chatMessageBuilder.buildUserMessage("A function must be called in this step"));
            return ASK_QUESTIONS;
        }

        ToolCallDto toolCall = chatResponse.toolCalls().get(0);
        log.info("Calling function '{}'", toolCall);

        String calledFunctionName = toolCall.functionCallProperties().name();
        Optional<FunctionCallResult> possibleFunctionCallResult = functionExecutionService.executeFunctionCall(toolCall);
        if (possibleFunctionCallResult.isEmpty()) {
            log.info("");
            sessionStateStore.appendMessage(chatResponse);
            var response = chatMessageBuilder.buildToolCallResponseMessage(toolCall.id(), new FunctionCallResponseDto(false, Map.of("errors", "Function '%s' does not exist".formatted(calledFunctionName))));
            sessionStateStore.appendMessage(response);
            return ASK_QUESTIONS;
        }

        FunctionCallResult functionCallResult = possibleFunctionCallResult.get();
        if (!functionCallResult.errors().isEmpty()) {
            log.warn("Errors when calling function '{}': '{}'", calledFunctionName, functionCallResult.errors());
            sessionStateStore.appendMessage(chatResponse);
            var response = chatMessageBuilder.buildToolCallResponseMessage(toolCall.id(), new FunctionCallResponseDto(false, Map.of("errors", functionCallResult.errors())));
            sessionStateStore.appendMessage(response);
            return ASK_QUESTIONS;
        }

        if (functionCallResult.messageToUser() != null) {
            chatResponse = chatResponse.withUserFacingContent(functionCallResult.messageToUser());
        }

        sessionStateStore.appendMessage(chatResponse);
        var response = chatMessageBuilder.buildToolCallResponseMessage(toolCall.id(), new FunctionCallResponseDto(true));
        sessionStateStore.appendMessage(response);

        if (calledFunctionName.equals(FinishAskingQuestionsFunction.FUNCTION_NAME)) {
            return REASON_ABOUT_TASKS_AND_PROCESS_FLOW;
        }

        return END;
    }
}
