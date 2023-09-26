package edu.agh.bpmnai.generator.openai;

import edu.agh.bpmnai.generator.Logging;
import edu.agh.bpmnai.generator.openai.model.ChatCompletionResponse;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import edu.agh.bpmnai.generator.openai.model.SingleChatResponse;

import java.util.*;

public class OpenAIChatSession {

    private final OpenAI.OpenAIModel usedModel;
    private final List<ChatMessage> messages;
    private final float temperature;

    private final OpenAIChatCompletionApi chatCompletionApi;

    private OpenAIChatSession(OpenAIChatCompletionApi chatCompletionApi, OpenAI.OpenAIModel modelToUse, float temperature, List<ChatMessage> messages) {
        this.chatCompletionApi = chatCompletionApi;
        this.usedModel = modelToUse;
        this.temperature = temperature;
        this.messages = new ArrayList<>(messages);
    }

    public static OpenAIChatSession newSession(OpenAIChatCompletionApi chatCompletionApi, OpenAI.OpenAIModel modelToUse, float initialTemperature) {
        return new OpenAIChatSession(chatCompletionApi, modelToUse, initialTemperature, new ArrayList<>());
    }

    public OpenAI.OpenAIModel getUsedModel() {
        return usedModel;
    }

    public float getTemperature() {
        return temperature;
    }

    public ChatMessage generateResponseFromPrompt(List<ChatMessage> prompt) {
        addMessages(prompt);
        ChatCompletionResponse chatCompletionResponse = chatCompletionApi.getChatCompletion(this);
        SingleChatResponse chatResponse = chatCompletionResponse.choices().get(0);
        ChatMessage responseMessage = chatResponse.message();

        if (responseMessage.getContent() != null) {
            Logging.logInfoMessage("Received response from the model", new Logging.ObjectToLog("Response content", chatResponse.message().getContent()));
        }

        addMessage(responseMessage);
        return responseMessage;
    }

    public ChatMessage generateResponseFromPrompt(List<ChatMessage> prompt, ChatCallableInterface callableInterface) {
        addMessages(prompt);

        boolean responseInProgress = true;
        ChatMessage responseMessage = null;
        while (responseInProgress) {
            ChatCompletionResponse chatCompletionResponse = chatCompletionApi.getChatCompletion(this, callableInterface.getCallableFunctions().stream().toList());
            SingleChatResponse chatResponse = chatCompletionResponse.choices().get(0);
            responseMessage = chatResponse.message();

            if (responseMessage.getContent() != null) {
                Logging.logInfoMessage("Received response from the model", new Logging.ObjectToLog("Response content", chatResponse.message().getContent()));
            }

            addMessage(responseMessage);

            if (chatResponse.finishReason().equals("function_call")) {
                Logging.logInfoMessage("Received function call from model", new Logging.ObjectToLog("Response function call", chatResponse.message().getFunctionCall()));
                Optional<ChatMessage> optionalResponseToModel = callableInterface.executeFunctionCall(responseMessage.getFunctionCall());
                optionalResponseToModel.ifPresent(this::addMessage);
            } else {
                responseInProgress = false;
            }
        }

        return responseMessage;
    }

    public void addMessage(ChatMessage message) {
        this.messages.add(message);
    }

    public void addMessages(Collection<ChatMessage> messages) {
        this.messages.addAll(messages);
    }

    public List<ChatMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenAIChatSession that = (OpenAIChatSession) o;
        return Float.compare(that.temperature, temperature) == 0 && usedModel == that.usedModel && Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usedModel, messages, temperature);
    }

    @Override
    public String toString() {
        return "OpenAIChatSession{" +
                "usedModel=" + usedModel +
                ", messages=" + messages +
                ", temperature=" + temperature +
                '}';
    }

}
