package edu.agh.bpmnai.generator.v2.session;

import edu.agh.bpmnai.generator.bpmn.BpmnToStringExporter;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.OpenAIChatCompletionApi;
import edu.agh.bpmnai.generator.v2.*;
import edu.agh.bpmnai.generator.v2.functions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.END;
import static edu.agh.bpmnai.generator.v2.session.SessionStatus.MODIFY_MODEL;

@Service
@Slf4j
public class ModifyModelState {
    public static final Set<ChatFunctionDto> FUNCTIONS_FOR_MODIFYING_THE_MODEL =
            Set.of(
                    AddSequenceOfTasksFunction.FUNCTION_DTO,
                    AddXorGatewayFunction.FUNCTION_DTO,
                    AddParallelGatewayFunction.FUNCTION_DTO,
                    AddWhileLoopFunction.FUNCTION_DTO
            );

    private final FunctionExecutionService functionExecutionService;

    private final OpenAIChatCompletionApi chatCompletionApi;

    private final OpenAI.OpenAIModel usedModel;

    private final SessionStateStore sessionStateStore;

    private final ConversationHistoryStore conversationHistoryStore;

    private final ChatMessageBuilder chatMessageBuilder;

    private final BpmnToStringExporter bpmnToStringExporter;

    @Autowired
    public ModifyModelState(
            FunctionExecutionService functionExecutionService,
            OpenAIChatCompletionApi chatCompletionApi,
            OpenAI.OpenAIModel usedModel,
            SessionStateStore sessionStateStore,
            ConversationHistoryStore conversationHistoryStore,
            ChatMessageBuilder chatMessageBuilder,
            BpmnToStringExporter bpmnToStringExporter
    ) {
        this.functionExecutionService = functionExecutionService;
        this.chatCompletionApi = chatCompletionApi;
        this.usedModel = usedModel;
        this.sessionStateStore = sessionStateStore;
        this.conversationHistoryStore = conversationHistoryStore;
        this.chatMessageBuilder = chatMessageBuilder;
        this.bpmnToStringExporter = bpmnToStringExporter;
    }

    public SessionStatus process(String userMessageContent, boolean isInitialPrompt) {
        sessionStateStore.appendMessage(chatMessageBuilder.buildUserMessage(
                "Use the provided functions to modify the diagram. After you're done, don't provide any messages to "
                + "the user\n" + "BEGIN REQUEST CONTEXT" + "\n" + "Current diagram state:\n"
                + bpmnToStringExporter.export(sessionStateStore.model()) + "\n" + "END REQUEST CONTEXT"));

        log.info("Request text sent to LLM: '{}'", sessionStateStore.lastAddedMessage());

        Set<ChatFunctionDto> availableFunctions = new HashSet<>(FUNCTIONS_FOR_MODIFYING_THE_MODEL);
        if (!isInitialPrompt) {
            availableFunctions.add(RemoveElementsFunction.FUNCTION_DTO);
            availableFunctions.add(RemoveSequenceFlowsFunction.FUNCTION_DTO);
        }

        ChatMessageDto chatResponse = chatCompletionApi.sendRequest(
                usedModel,
                sessionStateStore.messages(),
                availableFunctions,
                "auto"
        );
        sessionStateStore.appendMessage(chatResponse);
        if (chatResponse.toolCalls() == null || chatResponse.toolCalls().isEmpty()) {
            conversationHistoryStore.appendMessage(chatResponse.content());
            return END;
        }

        for (ToolCallDto toolCall : chatResponse.toolCalls()) {
            log.info("Calling function '{}'", toolCall);
            String calledFunctionName = toolCall.functionCallProperties().name();
            Result<String, CallError> functionCallResult = functionExecutionService.executeFunctionCall(toolCall);
            if (functionCallResult.isError()) {
                log.warn(
                        "Call of function '{}' returned error '{}'",
                        calledFunctionName,
                        functionCallResult.getError()
                );
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
                return MODIFY_MODEL;
            }

            var response = chatMessageBuilder.buildToolCallResponseMessage(
                    toolCall.id(),
                    new FunctionCallResponseDto(
                            true,
                            Map.of(
                                    "response",
                                    functionCallResult.getValue()
                            )
                    )
            );
            sessionStateStore.appendMessage(response);
        }

        return MODIFY_MODEL;
    }
}
