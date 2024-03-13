package edu.agh.bpmnai.generator.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.Logging;
import edu.agh.bpmnai.generator.openai.OpenAI.OpenAIModel;
import edu.agh.bpmnai.generator.openai.model.ChatCompletionRequest;
import edu.agh.bpmnai.generator.openai.model.ChatCompletionResponse;
import edu.agh.bpmnai.generator.openai.model.ChatFunction;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import edu.agh.bpmnai.generator.v2.ChatCompletionDto;
import edu.agh.bpmnai.generator.v2.ChatCompletionResponseDto;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import edu.agh.bpmnai.generator.v2.ChatToolDto;
import edu.agh.bpmnai.generator.v2.functions.ChatFunctionDto;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class OpenAIChatCompletionApi {

    private static final Bucket bucket;

    private final ObjectMapper objectMapper;

    static {
        Bandwidth limit = Bandwidth.simple(OpenAI.openAIApiTokenPerMinuteRateLimit, Duration.ofMinutes(1));
        bucket = Bucket.builder().addLimit(limit).build();
    }

    private final RestTemplate restTemplate;

    @Autowired
    public OpenAIChatCompletionApi(ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    public ChatMessageDto sendRequest(OpenAIModel modelToUse, List<ChatMessageDto> messages, @Nullable Set<ChatFunctionDto> availableFunctions, Object toolChoice) {
        var completionRequest = ChatCompletionDto.builder()
                .messages(messages)
                .model(modelToUse.getModelProperties().name())
                .tools(availableFunctions != null ? availableFunctions.stream().map(ChatToolDto::new).toList() : null)
                .toolChoice(toolChoice)
                .build();
        ChatCompletionResponseDto responseBody = getChatCompletion(completionRequest);
        return responseBody.choices().get(0).message();
    }

    public ChatCompletionResponse getChatCompletion(OpenAIChatSession conversation) throws FailedRequestException {
        OpenAIModel usedModel = conversation.getUsedModel();
        var requestBody = ChatCompletionRequest.builder()
                .model(usedModel.getModelProperties().name())
                .messages(conversation.getMessages())
                .temperature(conversation.getTemperature())
                .build();

        return sendRequest(usedModel, requestBody);
    }

    public ChatCompletionResponse getChatCompletion(OpenAIChatSession conversation, List<ChatFunction> callableFunctions) throws FailedRequestException {
        OpenAIModel usedModel = conversation.getUsedModel();
        var requestBody = ChatCompletionRequest.builder()
                .model(usedModel.getModelProperties().name())
                .messages(conversation.getMessages())
                .functions(callableFunctions)
                .temperature(conversation.getTemperature())
                .build();

        return sendRequest(usedModel, requestBody);
    }

    public ChatCompletionResponseDto getChatCompletion(ChatCompletionDto completionRequest) {
        HttpEntity<ChatCompletionDto> requestHttpEntity = prepareRequestHttpEntity(completionRequest);
        try {
            ResponseEntity<ChatCompletionResponseDto> response = restTemplate.postForEntity(
                    OpenAI.openAIApiUrl,
                    requestHttpEntity,
                    ChatCompletionResponseDto.class
            );

            return response.getBody();
        } catch (RestClientException e) {
            throw new FailedRequestException(e);
        }
    }

    private HttpEntity<ChatCompletionDto> prepareRequestHttpEntity(ChatCompletionDto requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + OpenAI.openAIApiKey);
        return new HttpEntity<>(requestBody, headers);
    }

    private ChatCompletionResponse sendRequest(OpenAIModel usedModel, ChatCompletionRequest requestBody) {
        Logging.logDebugMessage("Sending request to the OpenAI chat completion APU");
        HttpEntity<ChatCompletionRequest> requestHttpEntity = prepareRequestHttpEntity(requestBody);
        try {
            int usedTokens = calculateTokensUsed(requestBody, usedModel);
            bucket.asBlocking().consume(usedTokens);
            ResponseEntity<ChatCompletionResponse> response = restTemplate.postForEntity(
                    OpenAI.openAIApiUrl,
                    requestHttpEntity,
                    ChatCompletionResponse.class
            );

            Logging.logDebugMessage("Request was successful", new Logging.ObjectToLog("usedTokens", response.getBody().usage().total_tokens()));
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw new FailedRequestException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        throw new FailedRequestException("Request failed with no exception");
    }

    private HttpEntity<ChatCompletionRequest> prepareRequestHttpEntity(ChatCompletionRequest requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + OpenAI.openAIApiKey);
        return new HttpEntity<>(requestBody, headers);
    }

    private int calculateTokensUsed(ChatCompletionRequest request, OpenAIModel usedModel) {
        int numberOfTokensInMessages = 0;
        for (ChatMessage chatMessage : request.messages()) {
            numberOfTokensInMessages += calculateTokensUsedByMessage(chatMessage, usedModel);
        }
        int numberOfTokensInFunctionDescriptions;

        try {
            numberOfTokensInFunctionDescriptions = OpenAI.getNumberOfTokens(new ObjectMapper().writeValueAsString(request.functions()), usedModel);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return usedModel.getModelProperties().maxNumberOfTokens() - numberOfTokensInMessages - numberOfTokensInFunctionDescriptions;
    }

    private int calculateTokensUsedByMessage(ChatMessage message, OpenAIModel usedModel) {
        int tokensUsedByMessage = usedModel.getModelProperties().tokensPerMessage();

        tokensUsedByMessage += OpenAI.getNumberOfTokens(message.getRole().getRoleName(), usedModel);

        if (message.getContent() != null) {
            tokensUsedByMessage += OpenAI.getNumberOfTokens(message.getContent(), usedModel);
        }

        if (message.getName() != null) {
            tokensUsedByMessage += usedModel.getModelProperties().tokensPerName();
        }

        return tokensUsedByMessage;
    }

}
