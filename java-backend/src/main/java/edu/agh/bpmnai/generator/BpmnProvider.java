package edu.agh.bpmnai.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.Logging.ObjectToLog;
import edu.agh.bpmnai.generator.bpmn.BpmnElements;
import edu.agh.bpmnai.generator.bpmn.model.*;
import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.model.ChatCompletionRequest;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import edu.agh.bpmnai.generator.openai.model.ChatResponses;
import edu.agh.bpmnai.generator.openai.model.SingleChatResponse;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class BpmnProvider {

    private static final OpenAI.OpenAIModelProperties modelProperties = OpenAI.OpenAIModel.GPT_3_5_TURBO_16K.getModelProperties();

    private static final float temperature = 0.4f;

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final int messageTokenNumberCorrection = 50;

    private static final int multiplicativeBackoffStartingPoint = 200;

    private static ResponseEntity<ChatResponses> sendChatCompletionRequest(ChatCompletionRequest request) {
        Logging.logInfoMessage("Sending request", new ObjectToLog("requestBody", request));
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + OpenAI.openAIApiKey);

        HttpEntity<ChatCompletionRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        return restTemplate.exchange(
                OpenAI.openAIApiUrl,
                HttpMethod.POST,
                requestHttpEntity,
                ChatResponses.class
        );
    }

    private static Optional<FunctionCallError> parseModelFunctionCall(BpmnModelInstance bpmnModelInstance, ChatMessage responseMessage) throws JsonProcessingException {
        String functionName = responseMessage.function_call().get("name").asText();
        JsonNode functionArguments = responseMessage.function_call().get("arguments");
        switch (functionName) {
            case "addProcess" -> {
                BpmnProcess processParameters = mapper.readValue(functionArguments.asText(), BpmnProcess.class);
                if (doesIdExist(processParameters.id(), bpmnModelInstance)) {
                    return Optional.of(FunctionCallError.NON_UNIQUE_ID);
                }
                BpmnElements.addProcess(bpmnModelInstance, processParameters);
            }
            case "addStartEvent" -> {
                BpmnStartEvent startEventParameters = mapper.readValue(functionArguments.asText(), BpmnStartEvent.class);
                if (doesIdExist(startEventParameters.id(), bpmnModelInstance)) {
                    return Optional.of(FunctionCallError.NON_UNIQUE_ID);
                }
                BpmnElements.addStartEvent(bpmnModelInstance, startEventParameters);
            }
            case "addEndEvent" -> {
                BpmnEndEvent endEventParameters = mapper.readValue(functionArguments.asText(), BpmnEndEvent.class);
                if (doesIdExist(endEventParameters.id(), bpmnModelInstance)) {
                    return Optional.of(FunctionCallError.NON_UNIQUE_ID);
                }
                BpmnElements.addEndEvent(bpmnModelInstance, endEventParameters);
            }
            case "addUserTask" -> {
                BpmnUserTask userTaskParameters = mapper.readValue(functionArguments.asText(), BpmnUserTask.class);
                if (doesIdExist(userTaskParameters.id(), bpmnModelInstance)) {
                    return Optional.of(FunctionCallError.NON_UNIQUE_ID);
                }
                BpmnElements.addUserTask(bpmnModelInstance, userTaskParameters);
            }
            case "addGateway" -> {
                BpmnGateway gatewayParameters = mapper.readValue(functionArguments.asText(), BpmnGateway.class);
                if (doesIdExist(gatewayParameters.id(), bpmnModelInstance)) {
                    return Optional.of(FunctionCallError.NON_UNIQUE_ID);
                }
                BpmnElements.addGateway(bpmnModelInstance, gatewayParameters);
            }
            case "addSequenceFlow" -> {
                BpmnSequenceFlow sequenceFlowParameters = mapper.readValue(functionArguments.asText(), BpmnSequenceFlow.class);
                if (doesIdExist(sequenceFlowParameters.id(), bpmnModelInstance)) {
                    return Optional.of(FunctionCallError.NON_UNIQUE_ID);
                }
                BpmnElements.addSequenceFlow(bpmnModelInstance, sequenceFlowParameters);
            }
        }

        return Optional.empty();
    }

    private static boolean doesIdExist(String id, BpmnModelInstance modelInstance) {
        return modelInstance.getModelElementById(id) != null;
    }

    private static boolean isContinueConversation(ConversationStatus conversationStatus) {
        return conversationStatus != ConversationStatus.FINISHED && conversationStatus != ConversationStatus.UNHANDLED_ERROR;
    }

    private static ChatConversation carryOutConversation(BpmnModelInstance bpmnModelInstance, List<ChatMessage> messages) throws JsonProcessingException {
        int numberOfTokensInMessages = messages.stream()
                .mapToInt(chatMessage -> OpenAI.approximateTokensPerParagraph)
                .sum();

        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(
                modelProperties.name(),
                messages,
                BpmnElements.functionsDescriptions,
                temperature,
                modelProperties.maxNumberOfTokens() - numberOfTokensInMessages - BpmnElements.functionDescriptionsTokens
        );

        ResponseEntity<ChatResponses> httpResponseEntity = sendChatCompletionRequest(chatCompletionRequest);

        if (httpResponseEntity.getStatusCode() == HttpStatus.OK) {
            Logging.logInfoMessage("Request was successful", new ObjectToLog("requestBody", httpResponseEntity.getBody()));
        } else {
            Logging.logInfoMessage("Request failed", new ObjectToLog("statusCode", httpResponseEntity.getStatusCode()));
        }

        ChatResponses response = httpResponseEntity.getBody();
        int previousRequestUsedTokens = response.usage().total_tokens();
        SingleChatResponse chatResponse = httpResponseEntity.getBody().choices().get(0);
        ChatMessage responseMessage = chatResponse.message();

        ConversationStatus conversationStatus = ConversationStatus.CONTINUE;
        int numberOfTooManyTokensErrors = 0;
        while (isContinueConversation(conversationStatus)) {
            if (conversationStatus == ConversationStatus.ERROR_TOO_MANY_TOKENS_REQUESTED) {
                numberOfTooManyTokensErrors += 1;
                chatCompletionRequest = chatCompletionRequest.withMax_tokens(getNewValueOfMaxTokens(chatCompletionRequest.getMax_tokens(), numberOfTooManyTokensErrors));

            } else {
                if (responseMessage.content() == null) {
                    responseMessage.setContent("");
                }

                List<ChatMessage> requestMessages = new ArrayList<>(chatCompletionRequest.getMessages());
                requestMessages.add(responseMessage);

                if (responseMessage.function_call() != null) {
                    Optional<FunctionCallError> optionalFunctionCallError = parseModelFunctionCall(bpmnModelInstance, responseMessage);
                    optionalFunctionCallError.ifPresent(functionCallError -> requestMessages.add(handleIncorrectFunctionCall(functionCallError)));
                }

                chatCompletionRequest = chatCompletionRequest.withMessagesAndMax_Tokens(requestMessages, modelProperties.maxNumberOfTokens() - previousRequestUsedTokens - messageTokenNumberCorrection);
            }

            try {
                httpResponseEntity = sendChatCompletionRequest(chatCompletionRequest);
                Logging.logInfoMessage("Request was successful", new ObjectToLog("requestBody", httpResponseEntity.getBody()));
                response = httpResponseEntity.getBody();
                chatResponse = response.choices().get(0);
                responseMessage = chatResponse.message();
                previousRequestUsedTokens = response.usage().total_tokens();

                if (chatResponse.finish_reason().equals("stop")) {
                    Logging.logInfoMessage("Reached the end of the conversation");
                    conversationStatus = ConversationStatus.FINISHED;
                } else {
                    conversationStatus = ConversationStatus.CONTINUE;
                    numberOfTooManyTokensErrors = 0;
                }
            } catch (HttpClientErrorException.BadRequest badRequest) {
                badRequest.printStackTrace();
                conversationStatus = ConversationStatus.ERROR_TOO_MANY_TOKENS_REQUESTED;
            } catch (Exception e) {
                Logging.logThrowable("Request failed", e);
                conversationStatus = ConversationStatus.UNHANDLED_ERROR;
            }
        }

        return new ChatConversation(chatCompletionRequest.getMessages(), conversationStatus);
    }

    private static ChatMessage handleIncorrectFunctionCall(FunctionCallError functionCallError) {
        if (Objects.requireNonNull(functionCallError) == FunctionCallError.NON_UNIQUE_ID) {
            return ChatMessage.userMessage("The id used in the last function call was not globally unique. Please, call the function again with the same parameters and a new, globally unique id");
        }

        throw new UnhandledFunctionCallErrorException();
    }

    private static int getNewValueOfMaxTokens(int currentMaxTokens, int numberOfTooManyTokensErrors) {
        return currentMaxTokens - multiplicativeBackoffStartingPoint * numberOfTooManyTokensErrors;
    }

    public BpmnFile provideForTextPrompt(TextPrompt prompt) throws JsonProcessingException {
        BpmnModelInstance bpmnModelInstance = BpmnElements.getModelInstance();

        List<ChatMessage> messages = List.of(
                ChatMessage.systemMessage("When creating a BPMN model for the user, use only the provided functions"),
                ChatMessage.userMessage(prompt.content() + ". Start with the happy path.")
        );

        ChatConversation chatConversationForHappyPath = carryOutConversation(bpmnModelInstance, messages);

        if (chatConversationForHappyPath.status == ConversationStatus.FINISHED) {
            messages = new ArrayList<>(chatConversationForHappyPath.messages());
            messages.add(ChatMessage.userMessage("Now think about what problems may arise during the process and modify the model accordingly."));
            carryOutConversation(bpmnModelInstance, messages);
        }

        return new BpmnFile(Bpmn.convertToString(bpmnModelInstance));
    }

    private enum ConversationStatus {
        CONTINUE,
        FINISHED,
        ERROR_TOO_MANY_TOKENS_REQUESTED,
        UNHANDLED_ERROR
    }

    private enum FunctionCallError {
        NON_UNIQUE_ID
    }

    private record ChatConversation(List<ChatMessage> messages, ConversationStatus status) {

    }

    private static class UnhandledFunctionCallErrorException extends RuntimeException {
    }
}
