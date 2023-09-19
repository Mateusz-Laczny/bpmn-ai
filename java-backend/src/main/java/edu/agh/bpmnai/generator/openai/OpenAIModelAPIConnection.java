package edu.agh.bpmnai.generator.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.Logging;
import edu.agh.bpmnai.generator.openai.model.*;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

public class OpenAIModelAPIConnection {

    private static final int multiplicativeBackoffStartingPoint = 200;
    private final OpenAI.OpenAIModel usedModel;

    public OpenAIModelAPIConnection(OpenAI.OpenAIModel modelToUse) {
        this.usedModel = modelToUse;
    }

    private static boolean isRetryCommunication(CommunicationStatus communicationStatus) {
        return communicationStatus == CommunicationStatus.STARTING || communicationStatus == CommunicationStatus.RETRYING || communicationStatus == CommunicationStatus.TOO_MANY_TOKENS_REQUESTED;
    }

    private static int getNewValueOfMaxTokens(int currentMaxTokens, int numberOfTooManyTokensErrors) {
        return currentMaxTokens - multiplicativeBackoffStartingPoint * numberOfTooManyTokensErrors;
    }

    public ChatCompletionResponse sendChatCompletionRequest(List<ChatMessage> messages, List<ChatFunction> functionDescriptions, float temperature) throws ModelCommunicationException {
        int numberOfTokensInMessages = calculateTokensUsedByMessages(messages);
        int numberOfTokensInFunctionDescriptions;

        try {
            numberOfTokensInFunctionDescriptions = OpenAI.getNumberOfTokens(new ObjectMapper().writeValueAsString(functionDescriptions), usedModel);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        int startingMaxTokens = usedModel.getModelProperties().maxNumberOfTokens() - numberOfTokensInMessages - numberOfTokensInFunctionDescriptions;
        ChatCompletionRequest request = new ChatCompletionRequest(
                usedModel.getModelProperties().name(),
                messages,
                functionDescriptions,
                temperature,
                startingMaxTokens
        );

        int numberOfTooManyTokensErrors = 0;
        CommunicationStatus communicationStatus = CommunicationStatus.STARTING;
        Optional<ChatCompletionResponse> responseOptional = Optional.empty();

        while (isRetryCommunication(communicationStatus)) {
            if (communicationStatus == CommunicationStatus.TOO_MANY_TOKENS_REQUESTED) {
                numberOfTooManyTokensErrors += 1;
                request = request.withMax_tokens(getNewValueOfMaxTokens(request.getMax_tokens(), numberOfTooManyTokensErrors));
            }

            Logging.logInfoMessage("Sending request", new Logging.ObjectToLog("requestBody", request));
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.add("Authorization", "Bearer " + OpenAI.openAIApiKey);

            HttpEntity<ChatCompletionRequest> requestHttpEntity = new HttpEntity<>(request, headers);
            try {
                ResponseEntity<ChatCompletionResponse> response = restTemplate.exchange(
                        OpenAI.openAIApiUrl,
                        HttpMethod.POST,
                        requestHttpEntity,
                        ChatCompletionResponse.class
                );

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Logging.logInfoMessage("Request was successful", new Logging.ObjectToLog("requestBody", response.getBody()));
                    responseOptional = Optional.of(response.getBody());
                    communicationStatus = CommunicationStatus.SUCCESSFUL;
                } else {
                    Logging.logInfoMessage("Request status was different than OK", new Logging.ObjectToLog("statusCode", response.getStatusCode()));
                    communicationStatus = CommunicationStatus.UNHANDLED_ERROR;
                }
            } catch (HttpClientErrorException.BadRequest badRequest) {
                ApiErrorResponse errorResponse = badRequest.getResponseBodyAs(ApiErrorResponse.class);
                Logging.logInfoMessage("Request failed due to bad request content", new Logging.ObjectToLog("response", errorResponse));
                if (errorResponse.errorProperties().code().equals("context_length_exceeded")) {
                    communicationStatus = CommunicationStatus.TOO_MANY_TOKENS_REQUESTED;
                } else {
                    communicationStatus = CommunicationStatus.UNHANDLED_ERROR;
                }
            } catch (HttpStatusCodeException e) {
                Logging.logThrowable("Request failed", e);
                communicationStatus = CommunicationStatus.UNHANDLED_ERROR;
            }
        }

        if (responseOptional.isPresent()) {
            return responseOptional.get();
        }

        throw new ModelCommunicationException();
    }

    private int calculateTokensUsedByMessages(List<ChatMessage> messages) {
        return messages.stream()
                .mapToInt(chatMessage -> OpenAI.getNumberOfTokens(chatMessage.content(), usedModel))
                .sum();
    }

    private enum CommunicationStatus {
        STARTING,
        SUCCESSFUL,
        RETRYING,
        TOO_MANY_TOKENS_REQUESTED,
        UNHANDLED_ERROR
    }

    public static class ModelCommunicationException extends Exception {
    }
}
