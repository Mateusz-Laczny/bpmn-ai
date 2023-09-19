package edu.agh.bpmnai.generator;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.OpenAIModelAPIConnection;
import edu.agh.bpmnai.generator.openai.model.ChatCompletionResponse;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import edu.agh.bpmnai.generator.openai.model.SingleChatResponse;

import java.util.*;

class OpenAIChatConversation implements ChatConversation {

    private final OpenAI.OpenAIModel usedModel;
    private final List<ChatMessage> messages;
    private final float temperature;
    private ConversationStatus currentConversationStatus;

    private OpenAIChatConversation(OpenAI.OpenAIModel modelToUse, float temperature, List<ChatMessage> messages, ConversationStatus currentConversationStatus) {
        this.usedModel = modelToUse;
        this.temperature = temperature;
        this.messages = new ArrayList<>(messages);
        this.currentConversationStatus = currentConversationStatus;
    }

    public static OpenAIChatConversation emptyConversationWith(OpenAI.OpenAIModel modelToUse, float temperature) {
        return new OpenAIChatConversation(modelToUse, temperature, new ArrayList<>(), ConversationStatus.NEW);
    }

    private static void adjustModelResponseForFurtherUse(ChatMessage responseMessage) {
        if (responseMessage.content() == null) {
            responseMessage.setContent("");
        }
    }

    private static ChatMessage handleIncorrectFunctionCall(BpmnModel.FunctionCallError functionCallError) {
        if (functionCallError == BpmnModel.FunctionCallError.NON_UNIQUE_ID) {
            return ChatMessage.userMessage("The id used in the last function call was not globally unique. Please, call the function again with the same parameters and a new, globally unique id");
        } else if (functionCallError == BpmnModel.FunctionCallError.INVALID_PARAMETERS) {
            return ChatMessage.userMessage("The last function call was missing a parameter. Please, call the function again with all required parameters");
        }

        throw new UnhandledFunctionCallErrorException();
    }

    public void carryOutConversation(BpmnModel bpmnModel) {
        setCurrentConversationStatus(ConversationStatus.IN_PROGRESS);
        OpenAIModelAPIConnection apiConnection = new OpenAIModelAPIConnection(usedModel);

        while (isContinueConversation()) {
            try {
                ChatCompletionResponse chatCompletionResponse = apiConnection.sendChatCompletionRequest(getMessages(), BpmnModel.functionsDescriptions, temperature);
                SingleChatResponse chatResponse = chatCompletionResponse.choices().get(0);

                if (chatResponse.finish_reason().equals("stop")) {
                    Logging.logInfoMessage("Reached the end of the conversation");
                    currentConversationStatus = ConversationStatus.FINISHED;
                }

                ChatMessage responseMessage = chatResponse.message();
                adjustModelResponseForFurtherUse(responseMessage);
                addMessage(responseMessage);

                if (responseMessage.function_call() != null) {
                    Optional<BpmnModel.FunctionCallError> optionalFunctionCallError = bpmnModel.parseModelFunctionCall(responseMessage);
                    optionalFunctionCallError.ifPresent(functionCallError -> addMessage(handleIncorrectFunctionCall(functionCallError)));
                }
            } catch (OpenAIModelAPIConnection.ModelCommunicationException e) {
                Logging.logThrowable("Exception during communication with model API", e);
                setCurrentConversationStatus(ConversationStatus.UNHANDLED_ERROR);
            }
        }
    }

    @Override
    public void addMessage(ChatMessage message) {
        this.messages.add(message);
    }

    @Override
    public void addMessages(Collection<ChatMessage> messages) {
        this.messages.addAll(messages);
    }

    @Override
    public List<ChatMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public ConversationStatus getCurrentConversationStatus() {
        return currentConversationStatus;
    }

    @Override
    public void setCurrentConversationStatus(ConversationStatus currentConversationStatus) {
        this.currentConversationStatus = currentConversationStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (OpenAIChatConversation) obj;
        return Objects.equals(this.messages, that.messages) &&
                Objects.equals(this.currentConversationStatus, that.currentConversationStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messages, currentConversationStatus);
    }

    @Override
    public String toString() {
        return "OpenAIChatConversation[" +
                "messages=" + messages + ",\n" +
                "status=" + currentConversationStatus + ']';
    }

    private boolean isContinueConversation() {
        return currentConversationStatus != ConversationStatus.FINISHED && currentConversationStatus != ConversationStatus.UNHANDLED_ERROR;
    }

    private static class UnhandledFunctionCallErrorException extends RuntimeException {
    }
}
