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

import java.util.*;

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.*;

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
    private static final String PROMPT_TEMPLATE =
            """
            Use the provided functions to modify the diagram. After you're done, provide an empty message without \
            any text or function calls
            BEGIN REQUEST CONTEXT
            Current diagram state:
            %s
            END REQUEST CONTEXT""";
    private final FunctionExecutionService functionExecutionService;
    private final OpenAIChatCompletionApi chatCompletionApi;
    private final OpenAI.OpenAIModel usedModel;
    private final ConversationHistoryStore conversationHistoryStore;
    private final ChatMessageBuilder chatMessageBuilder;
    private final BpmnToStringExporter bpmnToStringExporter;

    @Autowired
    public ModifyModelState(
            FunctionExecutionService functionExecutionService,
            OpenAIChatCompletionApi chatCompletionApi,
            OpenAI.OpenAIModel usedModel,
            ConversationHistoryStore conversationHistoryStore,
            ChatMessageBuilder chatMessageBuilder,
            BpmnToStringExporter bpmnToStringExporter
    ) {
        this.functionExecutionService = functionExecutionService;
        this.chatCompletionApi = chatCompletionApi;
        this.usedModel = usedModel;
        this.conversationHistoryStore = conversationHistoryStore;
        this.chatMessageBuilder = chatMessageBuilder;
        this.bpmnToStringExporter = bpmnToStringExporter;
    }

    public ImmutableSessionState process(ImmutableSessionState sessionState) {
        ChatMessageDto promptDto =
                chatMessageBuilder.buildUserMessage(PROMPT_TEMPLATE.formatted(bpmnToStringExporter.export(sessionState)));
        List<ChatMessageDto> updatedModelContext = new ArrayList<>(sessionState.modelContext());
        updatedModelContext.add(promptDto);

        log.info("Request text sent to LLM: '{}'", promptDto);

        Set<ChatFunctionDto> availableFunctions = new HashSet<>(FUNCTIONS_FOR_MODIFYING_THE_MODEL);
        if (sessionState.sessionStatus() != NEW) {
            availableFunctions.add(RemoveNodesFunction.FUNCTION_DTO);
            availableFunctions.add(RemoveSequenceFlowsFunction.FUNCTION_DTO);
            availableFunctions.add(AddSequenceFlowsFunction.FUNCTION_DTO);
        }

        ChatMessageDto chatCompletion = chatCompletionApi.sendRequest(
                usedModel,
                updatedModelContext,
                availableFunctions,
                "auto"
        );

        updatedModelContext.add(chatCompletion);

        if (chatCompletion.toolCalls() == null || chatCompletion.toolCalls().isEmpty()) {
            conversationHistoryStore.appendMessage(chatCompletion.content());
            return ImmutableSessionState.builder().from(sessionState)
                    .sessionStatus(PROMPTING_FINISHED)
                    .modelContext(updatedModelContext)
                    .build();
        }

        for (ToolCallDto toolCall : chatCompletion.toolCalls()) {
            log.info("Calling function '{}'", toolCall);
            String calledFunctionName = toolCall.functionCallProperties().name();
            Result<FunctionCallResult, CallError> functionCallResult = functionExecutionService.executeFunctionCall(
                    toolCall,
                    sessionState
            );
            if (functionCallResult.isError()) {
                log.warn(
                        "Call of function '{}' returned error '{}'",
                        calledFunctionName,
                        functionCallResult.getError()
                );
                var errorResponse = chatMessageBuilder.buildToolCallResponseMessage(
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

                updatedModelContext.add(errorResponse);
            } else {
                var successResponse = chatMessageBuilder.buildToolCallResponseMessage(
                        toolCall.id(),
                        new FunctionCallResponseDto(
                                true,
                                Map.of(
                                        "response",
                                        functionCallResult.getValue()
                                )
                        )
                );

                updatedModelContext.add(successResponse);
                sessionState = functionCallResult.getValue().updatedSessionState();
            }
        }

        return ImmutableSessionState.builder().from(sessionState)
                .sessionStatus(MODIFY_MODEL)
                .modelContext(updatedModelContext)
                .build();
    }
}
