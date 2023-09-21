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
        String functionCallAsText = functionCallError.functionCallAsJson().asText();

        if (functionCallError.errorType() == BpmnModel.FunctionCallErrorType.NON_UNIQUE_ID) {
            return ChatMessage.userMessage("The id used in the last function call was not globally unique. Call the function again with the same parameters and a new, globally unique id. Invalid function call:\n" + functionCallAsText);
        } else if (functionCallError.errorType() == BpmnModel.FunctionCallErrorType.INVALID_PARAMETERS) {
            return ChatMessage.userMessage("The last function call had invalid parameters. Call the function again with proper arguments. Invalid function call:\n" + functionCallAsText);
        } else if (functionCallError.errorType() == BpmnModel.FunctionCallErrorType.MISSING_PARAMETERS) {
            return ChatMessage.userMessage("The last function call was missing one or more parameters. Call the function again with missing parameters added. Invalid function call:\n" + functionCallAsText);
        } else if (functionCallError.errorType() == BpmnModel.FunctionCallErrorType.ELEMENT_NOT_FOUND) {
            return ChatMessage.userMessage("The last function call contained id of an element that does not exist. Call the function again with a proper element id. Invalid function call:\n" + functionCallAsText);
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

                Logging.logInfoMessage("Received response from the model", new Logging.ObjectToLog("Response", chatResponse));

                if (chatResponse.finish_reason().equals("stop")) {
                    Logging.logInfoMessage("Reached the end of the conversation");
                    currentConversationStatus = ConversationStatus.FINISHED;
                }

                ChatMessage responseMessage = chatResponse.message();
                adjustModelResponseForFurtherUse(responseMessage);
                addMessage(responseMessage);

                if (responseMessage.function_call() != null) {
                    Optional<BpmnModel.FunctionCallError> optionalFunctionCallError = bpmnModel.parseModelFunctionCall(responseMessage);
                    if (optionalFunctionCallError.isPresent()) {
                        BpmnModel.FunctionCallError functionCallError = optionalFunctionCallError.get();
                        Logging.logWarnMessage("The chat function call could not be executed", new Logging.ObjectToLog("Chat response", chatResponse), new Logging.ObjectToLog("Error", functionCallError));
                        addMessage(handleIncorrectFunctionCall(functionCallError));
                    }
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

    private void setCurrentConversationStatus(ConversationStatus currentConversationStatus) {
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
