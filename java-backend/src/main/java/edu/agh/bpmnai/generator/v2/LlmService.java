package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.bpmn.layouting.BpmnSemanticLayouting;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.openai.OpenAI.OpenAIModel;
import edu.agh.bpmnai.generator.openai.OpenAIChatCompletionApi;
import edu.agh.bpmnai.generator.v2.functions.AddParallelGatewayFunction;
import edu.agh.bpmnai.generator.v2.functions.AddXorGatewayFunction;
import edu.agh.bpmnai.generator.v2.functions.ChatFunctionDto;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import edu.agh.bpmnai.generator.v2.functions.parameter.*;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;
import static edu.agh.bpmnai.generator.v2.session.SessionStatus.*;

@Service
@Slf4j
public class LlmService {

    public static final ChatFunctionDto IS_REQUEST_DESCRIPTION_DETAILED_ENOUGH = ChatFunctionDto.builder()
            .name("is_request_detailed_enough")
            .description("Checks if the user's request is detailed enough and asks for more details only if necessary.")
            .parameters(getSchemaForParametersDto(UserDescriptionReasoningDto.class))
            .build();
    private static final Set<ChatFunctionDto> chatFunctions = Set.of(
            IS_REQUEST_DESCRIPTION_DETAILED_ENOUGH,
            ChatFunctionDto.builder()
                    .name("add_sequence_of_activities")
                    .description("Adds a sequence of activities to the model, executed in a linear fashion (one after the other).")
                    .parameters(getSchemaForParametersDto(SequenceOfTasksDto.class))
                    .build(),
            AddXorGatewayFunction.FUNCTION_DTO,
            AddParallelGatewayFunction.FUNCTION_DTO,
            ChatFunctionDto.builder()
                    .name("add_while_loop")
                    .description("Adds a while loop to the model, where one or more activities can be executed multiple times, based on a condition")
                    .parameters(getSchemaForParametersDto(WhileLoopDto.class))
                    .build(),
            ChatFunctionDto.builder()
                    .name("add_if_else_branching")
                    .description("Adds an if-else branching to the model. If the condition is true, one branch is executed, if not another branch will be executed")
                    .parameters(getSchemaForParametersDto(IfElseBranchingDto.class))
                    .build(),
            ChatFunctionDto.builder()
                    .name("remove_activity")
                    .description("Removes an activity from the model. All activity predecessors will be connected to the activity successor")
                    .parameters(getSchemaForParametersDto(RemoveElementDto.class))
                    .build()
    );

    private final OpenAIChatCompletionApi openAIChatCompletionApi;

    private final OpenAIModel modelToUse = OpenAIModel.GPT_3_5_TURBO_16K;

    private final FunctionExecutionService functionExecutionService;

    private final SessionStateStorage sessionStateStorage;

    private final BpmnSemanticLayouting bpmnSemanticLayouting;

    @Autowired
    public LlmService(OpenAIChatCompletionApi openAIChatCompletionApi, FunctionExecutionService functionExecutionService, SessionStateStorage sessionStateStorage, BpmnSemanticLayouting bpmnSemanticLayouting) {
        this.openAIChatCompletionApi = openAIChatCompletionApi;
        this.functionExecutionService = functionExecutionService;
        this.sessionStateStorage = sessionStateStorage;
        this.bpmnSemanticLayouting = bpmnSemanticLayouting;
    }

    public UserRequestResponse getResponse(String userMessageContent) {
        SessionState sessionState = sessionStateStorage.getCurrentState();
        if (sessionState.sessionStatus() == TOOL_CALL_WAITING_FOR_USER_RESPONSE) {
            String callId = sessionState.getLastMessage().toolCalls().get(0).id();
            sessionState.appendToolResponse(callId, new FunctionCallResponseDto(true, Map.of("response", userMessageContent)));
        } else {
            sessionState.appendUserMessage(userMessageContent);
        }

        Object toolChoice = new ToolToCallDto(IS_REQUEST_DESCRIPTION_DETAILED_ENOUGH.name());

        boolean stillResponding = true;
        String responseForUser = null;
        while (stillResponding) {
            List<ChatMessageDto> messageDtos = new ArrayList<>(sessionState.systemMessages().stream()
                    .map(messageContent -> new ChatMessageDto("system", messageContent))
                    .toList());

            messageDtos.addAll(sessionState.messages());

            if (sessionState.sessionStatus() == IN_PROGRESS) {
                toolChoice = "auto";
            } else if (sessionState.sessionStatus() == NEW) {
                sessionState.setSessionStatus(IN_PROGRESS);
            }

            var completionRequest = ChatCompletionDto.builder()
                    .messages(messageDtos)
                    .model(modelToUse.getModelProperties().name())
                    .tools(chatFunctions.stream().map(ChatToolDto::new).toList())
                    .toolChoice(toolChoice)
                    .build();
            ChatCompletionResponseDto responseBody = openAIChatCompletionApi.getChatCompletion(completionRequest);
            ChatMessageDto chatResponse = responseBody.choices().get(0).message();
            sessionState.appendMessage(chatResponse);
            if (chatResponse.toolCalls() != null && !chatResponse.toolCalls().isEmpty()) {
                for (ToolCallDto toolCall : chatResponse.toolCalls()) {
                    log.info("Calling function '{}'", toolCall);
                    try {
                        FunctionCallResult functionCallResult = functionExecutionService.executeFunctionCall(sessionState, toolCall);
                        if (functionCallResult.errors().isEmpty()) {
                            if (functionCallResult.messageToUser() != null) {
                                stillResponding = false;
                                sessionState.setSessionStatus(TOOL_CALL_WAITING_FOR_USER_RESPONSE);
                                responseForUser = functionCallResult.messageToUser();
                            } else {
                                sessionState.appendToolResponse(toolCall.id(), new FunctionCallResponseDto(true));
                                sessionState.setSessionStatus(IN_PROGRESS);
                            }
                        } else {
                            sessionState.appendToolResponse(toolCall.id(), new FunctionCallResponseDto(false, Map.of("errors", functionCallResult.errors())));
                        }
                    } catch (NoExecutorException e) {
                        sessionState.appendToolResponse(toolCall.id(), new FunctionCallResponseDto(false, Map.of("errors", List.of("Tool '%s' does not exist".formatted(toolCall.id())))));
                    }
                }
            } else {
                sessionState.appendAssistantMessage(chatResponse.content());
                responseForUser = chatResponse.content();
                stillResponding = false;
            }
        }

        BpmnModel layoutedModel = bpmnSemanticLayouting.layoutModel(sessionState.model());
        return new UserRequestResponse(responseForUser, layoutedModel.asXmlString());
    }

    public void clearConversation() {
        sessionStateStorage.clearState();
    }
}
