package edu.agh.bpmnai.generator.v2.session;

import edu.agh.bpmnai.generator.bpmn.BpmnToStringExporter;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.OpenAIChatCompletionApi;
import edu.agh.bpmnai.generator.v2.*;
import edu.agh.bpmnai.generator.v2.functions.ChatFunctionDto;
import edu.agh.bpmnai.generator.v2.functions.DecideWhetherToUpdateTheDiagramFunction;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.*;

@Service
@Slf4j
public class DecideWhetherToModifyTheModelState {
    private static final Set<ChatFunctionDto> AVAILABLE_FUNCTIONS_IN_THIS_STATE = Set.of(
            DecideWhetherToUpdateTheDiagramFunction.FUNCTION_DTO
    );
    private static final String PROMPT_TEMPLATE = """
                                                  You will now be provided with a message from the user. The user can see the generated diagram.
                                                  Call the provided function to decide whether to update the diagram based on the request contents.
                                                  Do not provide an empty response.
                                                  BEGIN USER MESSAGE
                                                  %s
                                                  END USER MESSAGE

                                                  BEGIN REQUEST CONTEXT
                                                  Current diagram state:
                                                  %s
                                                  END REQUEST CONTEXT""";
    private final BpmnToStringExporter bpmnToStringExporter;
    private final FunctionExecutionService functionExecutionService;
    private final OpenAIChatCompletionApi chatCompletionApi;
    private final OpenAI.OpenAIModel usedModel;
    private final ChatMessageBuilder chatMessageBuilder;

    @Autowired
    public DecideWhetherToModifyTheModelState(
            BpmnToStringExporter bpmnToStringExporter,
            FunctionExecutionService functionExecutionService,
            OpenAIChatCompletionApi chatCompletionApi,
            OpenAI.OpenAIModel usedModel, ChatMessageBuilder chatMessageBuilder
    ) {
        this.bpmnToStringExporter = bpmnToStringExporter;
        this.functionExecutionService = functionExecutionService;
        this.chatCompletionApi = chatCompletionApi;
        this.usedModel = usedModel;
        this.chatMessageBuilder = chatMessageBuilder;
    }

    public ImmutableSessionState process(String userRequestContent, ImmutableSessionState sessionState) {
        String promptForModel =
                PROMPT_TEMPLATE.formatted(
                        userRequestContent,
                        bpmnToStringExporter.export(sessionState)
                );
        List<ChatMessageDto> updatedModelContext = new ArrayList<>(sessionState.modelContext());
        updatedModelContext.add(chatMessageBuilder.buildUserMessage(promptForModel));
        log.info("Request text sent to LLM: '{}'", promptForModel);

        ChatMessageDto chatCompletion = chatCompletionApi.sendRequest(
                usedModel,
                updatedModelContext,
                AVAILABLE_FUNCTIONS_IN_THIS_STATE,
                "auto"
        );

        updatedModelContext.add(chatCompletion);

        if (chatCompletion.toolCalls() == null) {
            log.warn("No tool calls");
            var response = chatMessageBuilder.buildUserMessage(
                    "You must call the provided function '%s' in this step".formatted(
                            DecideWhetherToUpdateTheDiagramFunction.FUNCTION_NAME));
            updatedModelContext.add(response);
            return ImmutableSessionState.builder().from(sessionState)
                    .sessionStatus(DECIDE_WHETHER_TO_MODIFY_THE_MODEL)
                    .modelContext(updatedModelContext)
                    .build();
        }

        ToolCallDto toolCall = chatCompletion.toolCalls().get(0);
        log.info("Calling function '{}'", toolCall);

        String calledFunctionName = toolCall.functionCallProperties().name();
        Result<FunctionCallResult, CallError> functionCallResult =
                functionExecutionService.executeFunctionCall(toolCall, sessionState);
        if (functionCallResult.isError()) {
            log.warn("Call of function '{}' returned error '{}'", calledFunctionName, functionCallResult.getError());
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
            return ImmutableSessionState.builder().from(sessionState)
                    .sessionStatus(DECIDE_WHETHER_TO_MODIFY_THE_MODEL)
                    .modelContext(updatedModelContext)
                    .build();
        }

        sessionState = functionCallResult.getValue().updatedSessionState();

        var successResponse = chatMessageBuilder.buildToolCallResponseMessage(
                toolCall.id(),
                new FunctionCallResponseDto(true)
        );
        updatedModelContext.add(successResponse);

        if (functionCallResult.getValue().responseToModel() != null) {
            return ImmutableSessionState.builder().from(sessionState)
                    .sessionStatus(PROMPTING_FINISHED_OK)
                    .modelContext(updatedModelContext)
                    .addUserFacingMessages(functionCallResult.getValue().responseToModel())
                    .build();
        }

        return ImmutableSessionState.builder().from(sessionState)
                .sessionStatus(REASON_ABOUT_TASKS_AND_PROCESS_FLOW)
                .modelContext(updatedModelContext)
                .build();
    }
}
