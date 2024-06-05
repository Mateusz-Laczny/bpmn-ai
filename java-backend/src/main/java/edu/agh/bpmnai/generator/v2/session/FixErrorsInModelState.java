package edu.agh.bpmnai.generator.v2.session;

import edu.agh.bpmnai.generator.bpmn.BpmnToStringExporter;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.OpenAIChatCompletionApi;
import edu.agh.bpmnai.generator.v2.*;
import edu.agh.bpmnai.generator.v2.functions.AddSequenceFlowsFunction;
import edu.agh.bpmnai.generator.v2.functions.ChatFunctionDto;
import edu.agh.bpmnai.generator.v2.functions.RemoveNodesFunction;
import edu.agh.bpmnai.generator.v2.functions.RemoveSequenceFlowsFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.END;
import static edu.agh.bpmnai.generator.v2.session.SessionStatus.FIX_ERRORS;

@Service
@Slf4j
public class FixErrorsInModelState {
    public static final Set<ChatFunctionDto> AVAILABLE_FUNCTIONS = Set.of(
            RemoveSequenceFlowsFunction.FUNCTION_DTO, RemoveNodesFunction.FUNCTION_DTO,
            AddSequenceFlowsFunction.FUNCTION_DTO
    );
    private final SessionStateStore sessionStateStore;
    private final ChatMessageBuilder chatMessageBuilder;
    private final BpmnToStringExporter bpmnToStringExporter;
    private final OpenAIChatCompletionApi chatCompletionApi;
    private final OpenAI.OpenAIModel usedModel;
    private final FunctionExecutionService functionExecutionService;

    @Autowired
    public FixErrorsInModelState(
            SessionStateStore sessionStateStore,
            ChatMessageBuilder chatMessageBuilder,
            BpmnToStringExporter bpmnToStringExporter,
            OpenAIChatCompletionApi chatCompletionApi,
            OpenAI.OpenAIModel usedModel,
            FunctionExecutionService functionExecutionService
    ) {
        this.sessionStateStore = sessionStateStore;
        this.chatMessageBuilder = chatMessageBuilder;
        this.bpmnToStringExporter = bpmnToStringExporter;
        this.chatCompletionApi = chatCompletionApi;
        this.usedModel = usedModel;
        this.functionExecutionService = functionExecutionService;
    }

    public SessionStatus process(String userMessageContent) {
        sessionStateStore.appendMessage(chatMessageBuilder.buildUserMessage(
                "Think about possible errors in the model, and if there are any fix them using only the provided "
                + "functions. If there are no errors respond \"No errors\"\n"
                + "BEGIN REQUEST CONTEXT" + "\n" + "Current diagram state:\n" + bpmnToStringExporter.export() + "\n"
                + "END REQUEST CONTEXT"));
        log.info("Request text sent to LLM: '{}'", sessionStateStore.lastAddedMessage());
        ChatMessageDto chatResponse = chatCompletionApi.sendRequest(
                usedModel, sessionStateStore.messages(), AVAILABLE_FUNCTIONS, "auto");
        sessionStateStore.appendMessage(chatResponse);
        if (chatResponse.toolCalls() == null || chatResponse.toolCalls().isEmpty()) {
            return END;
        }

        for (ToolCallDto toolCall : chatResponse.toolCalls()) {
            log.info("Calling function '{}'", toolCall);
            String calledFunctionName = toolCall.functionCallProperties().name();
            Result<String, CallError> functionCallResult = functionExecutionService.executeFunctionCall(toolCall);
            if (functionCallResult.isError()) {
                log.warn(
                        "Call of function '{}' returned error '{}'", calledFunctionName, functionCallResult.getError());
                var response = chatMessageBuilder.buildToolCallResponseMessage(
                        toolCall.id(), new FunctionCallResponseDto(false, Map.of("errors", functionCallResult.getError()
                                .message())));
                sessionStateStore.appendMessage(response);
                return FIX_ERRORS;
            }

            var response = chatMessageBuilder.buildToolCallResponseMessage(
                    toolCall.id(), new FunctionCallResponseDto(
                            true,
                            Map.of("response", functionCallResult.getValue())
                    ));
            sessionStateStore.appendMessage(response);
        }

        return FIX_ERRORS;
    }
}
