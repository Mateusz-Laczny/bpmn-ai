package edu.agh.bpmnai.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.BpmnElements;
import edu.agh.bpmnai.generator.bpmn.model.*;
import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import edu.agh.bpmnai.generator.openai.model.ChatRequest;
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

@Service
public class BpmnProvider {

    private static final OpenAI.OpenAIModelProperties modelProperties = OpenAI.OpenAIModel.GPT_3_5_TURBO_16K.getModelProperties();

    private static final float temperature = 0.4f;

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final int messageTokenNumberCorrection = 50;

    private static ResponseEntity<ChatResponses> sendChatCompletionRequest(ChatRequest request) {
        Logging.logInfoMessage("Sending request", new Logging.ObjectToLog("requestBody", request));
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + OpenAI.openAIApiKey);

        HttpEntity<ChatRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        return restTemplate.exchange(
                OpenAI.openAIApiUrl,
                HttpMethod.POST,
                requestHttpEntity,
                ChatResponses.class
        );
    }

    private static void parseModelFunctionCall(BpmnModelInstance bpmnModelInstance, ChatMessage responseMessage) throws JsonProcessingException {
        String functionName = responseMessage.function_call().get("name").asText();
        JsonNode functionArguments = responseMessage.function_call().get("arguments");
        System.out.println(functionArguments);
        switch (functionName) {
            case "addProcess" ->
                    BpmnElements.addProcess(bpmnModelInstance, mapper.readValue(functionArguments.asText(), BpmnProcess.class));
            case "addStartEvent" ->
                    BpmnElements.addStartEvent(bpmnModelInstance, mapper.readValue(functionArguments.asText(), BpmnStartEvent.class));
            case "addEndEvent" ->
                    BpmnElements.addEndEvent(bpmnModelInstance, mapper.readValue(functionArguments.asText(), BpmnEndEvent.class));
            case "addUserTask" ->
                    BpmnElements.addUserTask(bpmnModelInstance, mapper.readValue(functionArguments.asText(), BpmnUserTask.class));
            case "addSequenceFlow" ->
                    BpmnElements.addSequenceFlow(bpmnModelInstance, mapper.readValue(functionArguments.asText(), BpmnSequenceFlow.class));
        }
    }

    private static boolean isContinueConversation(ConversationStatus conversationStatus) {
        return conversationStatus != ConversationStatus.STOP && conversationStatus != ConversationStatus.UNHANDLED_ERROR;
    }

    public BpmnFile provideForTextPrompt(TextPrompt prompt) throws JsonProcessingException {
        BpmnModelInstance bpmnModelInstance = BpmnElements.getModelInstance();

        List<ChatMessage> messages = new ArrayList<>(List.of(
                ChatMessage.systemMessage("When creating a BPMN model for the user, use only the provided functions"),
                ChatMessage.userMessage(prompt.content())
        ));

        int numberOfTokensInMessages = messages.stream()
                .mapToInt(chatMessage -> OpenAI.approximateTokensPerParagraph)
                .sum();

        ChatRequest request = new ChatRequest(
                modelProperties.name(),
                messages,
                BpmnElements.functionsDescriptions,
                temperature,
                modelProperties.maxNumberOfTokens() - numberOfTokensInMessages - BpmnElements.functionDescriptionsTokens
        );

        ResponseEntity<ChatResponses> httpResponseEntity = sendChatCompletionRequest(request);

        if (httpResponseEntity.getStatusCode() == HttpStatus.OK) {
            Logging.logInfoMessage("Request successful", new Logging.ObjectToLog("requestBody", httpResponseEntity.getBody()));
        } else {
            Logging.logInfoMessage("Request failed", new Logging.ObjectToLog("statusCode", httpResponseEntity.getStatusCode()));
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
                request = request.withMax_tokens(request.getMax_tokens() - 200 * numberOfTooManyTokensErrors);

            } else {
                if (responseMessage.content() == null) {
                    responseMessage.setContent("");
                }

                if (responseMessage.function_call() != null) {
                    parseModelFunctionCall(bpmnModelInstance, responseMessage);
                }

                List<ChatMessage> requestMessages = new ArrayList<>(request.getMessages());
                requestMessages.add(responseMessage);
                request = request.withMessagesAndMax_Tokens(requestMessages, modelProperties.maxNumberOfTokens() - previousRequestUsedTokens - messageTokenNumberCorrection);
            }

            try {
                httpResponseEntity = sendChatCompletionRequest(request);
                Logging.logInfoMessage("Request successful\n", new Logging.ObjectToLog("requestBody", httpResponseEntity.getBody()));
                response = httpResponseEntity.getBody();
                chatResponse = response.choices().get(0);
                responseMessage = chatResponse.message();
                previousRequestUsedTokens = response.usage().total_tokens();

                if (chatResponse.finish_reason().equals("stop")) {
                    Logging.logInfoMessage("Reached the end of the conversation");
                    conversationStatus = ConversationStatus.STOP;
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


        return new BpmnFile(Bpmn.convertToString(bpmnModelInstance));
    }

    private enum ConversationStatus {
        CONTINUE,
        STOP,
        ERROR_TOO_MANY_TOKENS_REQUESTED,
        UNHANDLED_ERROR
    }
}
