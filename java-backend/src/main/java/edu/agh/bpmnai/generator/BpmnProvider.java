package edu.agh.bpmnai.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class BpmnProvider {

    private static final String openAIApiUrl = "https://api.openai.com/v1/chat/completions";

    private static final String modelName = "gpt-3.5-turbo-16k";

    private static final float temperature = 0.4f;

    private final static int maxNumberOfTokens = 12000;

    private static final String openAiApiKey = System.getenv("OPENAI_API_KEY");

    private static ResponseEntity<ChatResponses> sendChatCompletionRequest(ChatRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + openAiApiKey);

        HttpEntity<ChatRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        return restTemplate.exchange(
                openAIApiUrl,
                HttpMethod.POST,
                requestHttpEntity,
                ChatResponses.class
        );
    }

    public BpmnFile provideForTextPrompt(TextPrompt prompt) throws JsonProcessingException {
        List<ChatMessage> messages = new ArrayList<>(List.of(
                ChatMessage.systemMessage("When creating a BPMN model for the user, use only the provided functions"),
                ChatMessage.userMessage(prompt.content())
        ));

        ChatRequest request = new ChatRequest(
                modelName,
                messages,
                BpmnElements.functionsDescriptions,
                temperature,
                maxNumberOfTokens
        );

        System.out.println(new ObjectMapper().writeValueAsString(request));

        ResponseEntity<ChatResponses> response = sendChatCompletionRequest(request);

        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Request successful");
            System.out.println(response.getBody());
        } else {
            System.out.println("Request failed");
            System.out.println(response.getStatusCode());
        }

        SingleChatResponse chatResponse = response.getBody().choices().get(0);
        ChatMessage responseMessage = chatResponse.message();

        while (!chatResponse.finish_reason().equals("stop")) {
            if (responseMessage.content() == null) {
                responseMessage.setContent("");
            }

            request.messages().add(responseMessage);
            request.messages().add(ChatMessage.functionMessage(responseMessage.function_call().get("name").asText() + " called", responseMessage.function_call().get("name").asText()));

            System.out.println(request);

            response = sendChatCompletionRequest(request);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Request successful");
                System.out.println(response.getBody());
            } else {
                System.out.println("Request failed");
                System.out.println(response.getStatusCode());
            }

            chatResponse = response.getBody().choices().get(0);
            responseMessage = chatResponse.message();
        }

        return new BpmnFile("aa");
    }
}
