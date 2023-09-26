package edu.agh.bpmnai.generator.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.Logging;
import edu.agh.bpmnai.generator.openai.model.ChatCompletionRequest;
import edu.agh.bpmnai.generator.openai.model.ChatCompletionResponse;
import edu.agh.bpmnai.generator.openai.model.ChatFunction;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

@Service
public class OpenAIChatCompletionApi {

    private static final Bucket bucket;

    static {
        Bandwidth limit = Bandwidth.simple(OpenAI.openAIApiTokenPerMinuteRateLimit, Duration.ofMinutes(1));
        bucket = Bucket.builder().addLimit(limit).build();
    }

    private final RestTemplate restTemplate;

    @Autowired
    public OpenAIChatCompletionApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ChatCompletionResponse getChatCompletion(OpenAIChatSession conversation) throws FailedRequestException {
        OpenAI.OpenAIModel usedModel = conversation.getUsedModel();
        var requestBody = ChatCompletionRequest.builder()
                .model(usedModel.getModelProperties().name())
                .messages(conversation.getMessages())
                .temperature(conversation.getTemperature())
                .build();

        return sendRequest(usedModel, requestBody);
    }

    public ChatCompletionResponse getChatCompletion(OpenAIChatSession conversation, List<ChatFunction> callableFunctions) throws FailedRequestException {
        OpenAI.OpenAIModel usedModel = conversation.getUsedModel();
        var requestBody = ChatCompletionRequest.builder()
                .model(usedModel.getModelProperties().name())
                .messages(conversation.getMessages())
                .functions(callableFunctions)
                .temperature(conversation.getTemperature())
                .build();

        return sendRequest(usedModel, requestBody);
    }

    private ChatCompletionResponse sendRequest(OpenAI.OpenAIModel usedModel, ChatCompletionRequest requestBody) {
        Logging.logDebugMessage("Sending request", new Logging.ObjectToLog("requestBody", requestBody));
        HttpEntity<ChatCompletionRequest> requestHttpEntity = prepareRequestHttpEntity(requestBody);
        try {
            int usedTokens = calculateTokensUsed(requestBody, usedModel);
            bucket.asBlocking().consume(usedTokens);
            ResponseEntity<ChatCompletionResponse> response = restTemplate.postForEntity(
                    OpenAI.openAIApiUrl,
                    requestHttpEntity,
                    ChatCompletionResponse.class
            );

            Logging.logDebugMessage("Request was successful", new Logging.ObjectToLog("requestBody", response.getBody()));
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

    private int calculateTokensUsed(ChatCompletionRequest request, OpenAI.OpenAIModel usedModel) {
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

    private int calculateTokensUsedByMessage(ChatMessage message, OpenAI.OpenAIModel usedModel) {
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
