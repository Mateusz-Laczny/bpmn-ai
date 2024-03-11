package edu.agh.bpmnai.generator.v2.session;

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

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.END;
import static edu.agh.bpmnai.generator.v2.session.SessionStatus.MODIFY_MODEL;

@Service
@Slf4j
public class ModifyModelState {
    public static final Set<ChatFunctionDto> FUNCTIONS_FOR_MODIFYING_THE_MODEL = Set.of(
            AddSequenceOfTasksFunction.FUNCTION_DTO,
            AddXorGatewayFunction.FUNCTION_DTO,
            AddParallelGatewayFunction.FUNCTION_DTO,
            AddWhileLoopFunction.FUNCTION_DTO,
            AddIfElseBranchingFunction.FUNCTION_DTO,
            RemoveElementFunction.FUNCTION_DTO
    );

    private final FunctionExecutionService functionExecutionService;

    private final OpenAIChatCompletionApi chatCompletionApi;

    private final OpenAI.OpenAIModel usedModel;

    private final SessionStateStore sessionStateStore;

    private final ChatMessageBuilder chatMessageBuilder;

    @Autowired
    public ModifyModelState(FunctionExecutionService functionExecutionService, OpenAIChatCompletionApi chatCompletionApi, OpenAI.OpenAIModel usedModel, SessionStateStore sessionStateStore, ChatMessageBuilder chatMessageBuilder) {
        this.functionExecutionService = functionExecutionService;
        this.chatCompletionApi = chatCompletionApi;
        this.usedModel = usedModel;
        this.sessionStateStore = sessionStateStore;
        this.chatMessageBuilder = chatMessageBuilder;
    }

    public SessionStatus process(String userMessageContent) {
        sessionStateStore.appendMessage(chatMessageBuilder.buildUserMessage(userMessageContent));
        ChatMessageDto chatResponse = chatCompletionApi.sendRequest(usedModel, sessionStateStore.messages(), FUNCTIONS_FOR_MODIFYING_THE_MODEL, "auto");
        sessionStateStore.appendMessage(chatResponse);
        if (chatResponse.toolCalls() == null || chatResponse.toolCalls().isEmpty()) {
            log.debug("No tool call, changing state to '{}'", END);
            var response = chatMessageBuilder.buildAssistantMessage(chatResponse.content());
            sessionStateStore.appendMessage(response);
            return END;
        }

        for (ToolCallDto toolCall : chatResponse.toolCalls()) {
            log.info("Calling function '{}'", toolCall);
            String calledFunctionName = toolCall.functionCallProperties().name();
            Optional<FunctionCallResult> possibleFunctionCallResult = functionExecutionService.executeFunctionCall(toolCall);
            if (possibleFunctionCallResult.isEmpty()) {
                var response = chatMessageBuilder.buildToolCallResponseMessage(toolCall.id(), new FunctionCallResponseDto(false, Map.of("errors", "Function '%s' does not exist".formatted(calledFunctionName))));
                sessionStateStore.appendMessage(response);
                return MODIFY_MODEL;
            }

            FunctionCallResult functionCallResult = possibleFunctionCallResult.get();
            if (!functionCallResult.errors().isEmpty()) {
                log.info("Errors when calling function '{}': '{}'", calledFunctionName, functionCallResult.errors());
                var response = chatMessageBuilder.buildToolCallResponseMessage(toolCall.id(), new FunctionCallResponseDto(false, Map.of("errors", functionCallResult.errors())));
                sessionStateStore.appendMessage(response);
                return MODIFY_MODEL;
            }

            var response = chatMessageBuilder.buildToolCallResponseMessage(toolCall.id(), new FunctionCallResponseDto(true));
            sessionStateStore.appendMessage(response);
        }

        return MODIFY_MODEL;
    }
}
