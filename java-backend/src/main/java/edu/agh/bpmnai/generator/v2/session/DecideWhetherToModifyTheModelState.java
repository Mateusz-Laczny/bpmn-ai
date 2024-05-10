package edu.agh.bpmnai.generator.v2.session;

import edu.agh.bpmnai.generator.bpmn.BpmnToStringExporter;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.OpenAIChatCompletionApi;
import edu.agh.bpmnai.generator.v2.*;
import edu.agh.bpmnai.generator.v2.functions.ChatFunctionDto;
import edu.agh.bpmnai.generator.v2.functions.FillInMissingDetailsInUserRequestFunction;
import edu.agh.bpmnai.generator.v2.functions.RespondWithoutModifyingDiagramFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.*;

@Service
@Slf4j
public class DecideWhetherToModifyTheModelState {
    private static final Set<ChatFunctionDto> AVAILABLE_FUNCTIONS_IN_THIS_STATE = Set.of(
            RespondWithoutModifyingDiagramFunction.FUNCTION_DTO,
            FillInMissingDetailsInUserRequestFunction.FUNCTION_DTO
    );

    private final SessionStateStore sessionStateStore;

    private final ConversationHistoryStore conversationHistoryStore;

    private final BpmnToStringExporter bpmnToStringExporter;

    private final FunctionExecutionService functionExecutionService;

    private final OpenAIChatCompletionApi chatCompletionApi;

    private final OpenAI.OpenAIModel usedModel;

    private final ChatMessageBuilder chatMessageBuilder;

    @Autowired
    public DecideWhetherToModifyTheModelState(
            SessionStateStore sessionStateStore,
            ConversationHistoryStore conversationHistoryStore, BpmnToStringExporter bpmnToStringExporter,
            FunctionExecutionService functionExecutionService,
            OpenAIChatCompletionApi chatCompletionApi,
            OpenAI.OpenAIModel usedModel, ChatMessageBuilder chatMessageBuilder
    ) {
        this.sessionStateStore = sessionStateStore;
        this.conversationHistoryStore = conversationHistoryStore;
        this.bpmnToStringExporter = bpmnToStringExporter;
        this.functionExecutionService = functionExecutionService;
        this.chatCompletionApi = chatCompletionApi;
        this.usedModel = usedModel;
        this.chatMessageBuilder = chatMessageBuilder;
    }

    public SessionStatus process(String userRequestContent) {
        String promptForModel =
                "Decide whether to modify the diagram based on current state and user request\n"
                + "BEGIN USER REQUEST\n"
                + userRequestContent
                + "END USER REQUEST\n"
                + "\n"
                + "BEGIN REQUEST CONTEXT\n"
                + "Current diagram state:\n" + bpmnToStringExporter.export(sessionStateStore.model())
                + "\n"
                + "END REQUEST CONTEXT";
        log.info("Request text sent to LLM: '{}'", promptForModel);

        ChatMessageDto chatResponse = chatCompletionApi.sendRequest(
                usedModel,
                sessionStateStore.messages(),
                AVAILABLE_FUNCTIONS_IN_THIS_STATE,
                "auto"
        );

        sessionStateStore.appendMessage(chatResponse);

        if (chatResponse.toolCalls() == null) {
            log.warn("No function calls even though a function call was expected");
            sessionStateStore.appendMessage(chatMessageBuilder.buildUserMessage(
                    "Use the provided functions in your response, do not provide a response using other means"));
            return DECIDE_WHETHER_TO_MODIFY_THE_MODEL;
        }

        ToolCallDto toolCall = chatResponse.toolCalls().get(0);
        log.info("Calling function '{}'", toolCall);

        String calledFunctionName = toolCall.functionCallProperties().name();
        Result<String, CallError> functionCallResult = functionExecutionService.executeFunctionCall(toolCall);
        if (functionCallResult.isError()) {
            log.warn("Call of function '{}' returned error '{}'", calledFunctionName, functionCallResult.getError());
            var response = chatMessageBuilder.buildToolCallResponseMessage(
                    toolCall.id(),
                    new FunctionCallResponseDto(
                            false,
                            Map.of(
                                    "errors",
                                    functionCallResult.getError()
                                            .message()
                            )
                    )
            );
            sessionStateStore.appendMessage(response);
            return DECIDE_WHETHER_TO_MODIFY_THE_MODEL;
        }

        if (calledFunctionName.equals(RespondWithoutModifyingDiagramFunction.FUNCTION_NAME)) {
            conversationHistoryStore.appendMessage(functionCallResult.getValue());
        }

        var response = chatMessageBuilder.buildToolCallResponseMessage(
                toolCall.id(),
                new FunctionCallResponseDto(true)
        );
        sessionStateStore.appendMessage(response);

        if (calledFunctionName.equals(FillInMissingDetailsInUserRequestFunction.FUNCTION_NAME)) {
            return REASON_ABOUT_TASKS_AND_PROCESS_FLOW;
        }

        return END;

    }
}
