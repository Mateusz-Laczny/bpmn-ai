package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.openai.OpenAI.OpenAIModel;
import edu.agh.bpmnai.generator.openai.OpenAIChatCompletionApi;
import edu.agh.bpmnai.generator.v2.functions.ChatFunctionDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class LlmService {

    private final OpenAIChatCompletionApi openAIChatCompletionApi;

    private final OpenAIModel modelToUse = OpenAIModel.GPT_3_5_TURBO_16K;

    private final FunctionExecutionService functionExecutionService;

    @Autowired
    public LlmService(OpenAIChatCompletionApi openAIChatCompletionApi, FunctionExecutionService functionExecutionService) {
        this.openAIChatCompletionApi = openAIChatCompletionApi;
        this.functionExecutionService = functionExecutionService;
    }

    public void getResponse(SessionState sessionState, Set<ChatFunctionDto> chatFunctions) {
        boolean stillResponding = true;
        while (stillResponding) {
            List<ChatMessageDto> messageDtos = new ArrayList<>(sessionState.systemMessages().stream()
                    .map(messageContent -> new ChatMessageDto("system", messageContent))
                    .toList());

            messageDtos.addAll(sessionState.messages());

            var completionRequest = ChatCompletionDto.builder()
                    .messages(messageDtos)
                    .model(modelToUse.getModelProperties().name())
                    .tools(chatFunctions.stream().map(ChatToolDto::new).toList())
                    .toolChoice("auto")
                    .build();
            ChatCompletionResponseDto responseBody = openAIChatCompletionApi.getChatCompletion(completionRequest);
            ChatMessageDto chatResponse = responseBody.choices().get(0).message();
            sessionState.appendMessage(chatResponse);
            if (chatResponse.toolCalls() != null && !chatResponse.toolCalls().isEmpty()) {
                for (ToolCallDto toolCall : chatResponse.toolCalls()) {
                    log.info("Calling function '{}'", toolCall);
                    try {
                        boolean needsResponseFromUser = functionExecutionService.executeFunctionCall(sessionState, toolCall);
                        if (needsResponseFromUser) {
                            stillResponding = false;
                        }
                    } catch (NoExecutorException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                sessionState.appendAssistantMessage(chatResponse.content());
                stillResponding = false;
            }
        }
    }
}
