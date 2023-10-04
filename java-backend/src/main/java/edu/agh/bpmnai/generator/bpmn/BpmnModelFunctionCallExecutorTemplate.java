package edu.agh.bpmnai.generator.bpmn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.Logging;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;

import java.util.Optional;
import java.util.function.Function;

abstract class BpmnModelFunctionCallExecutorTemplate<T> implements Function<JsonNode, Optional<ChatMessage>> {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final Class<T> callArgumentsClass;

    public BpmnModelFunctionCallExecutorTemplate(Class<T> callArgumentsClass) {
        this.callArgumentsClass = callArgumentsClass;
    }

    @Override
    public final Optional<ChatMessage> apply(JsonNode callArguments) {
        T callArgumentsPojo;
        try {
            callArgumentsPojo = parseCallArguments(callArguments);
        } catch (JsonProcessingException e) {
            Logging.logThrowable("Error when parsing function call arguments", e);
            ChatMessage errorMessage = ChatMessage.userMessage("The last function call parameters were invalid. Call the function again with proper arguments. Error: " + e.getMessage());
            return Optional.of(errorMessage);
        }

        Optional<ChatMessage> optionalArgumentVerificationError = verifyCallArguments(callArgumentsPojo);
        if (optionalArgumentVerificationError.isPresent()) {
            return optionalArgumentVerificationError;
        }

        Optional<ChatMessage> messageAfterCallExecution;
        try {
            messageAfterCallExecution = executeFunctionCall(callArgumentsPojo);
        } catch (IllegalArgumentException e) {
            Logging.logThrowable("Error when executing function call", e);
            return Optional.of(ChatMessage.userMessage(e.getMessage()));
        }
        return messageAfterCallExecution;
    }

    protected abstract Optional<ChatMessage> executeFunctionCall(T callArgumentsPojo);

    protected Optional<ChatMessage> verifyCallArguments(T callArgumentsPojo) {
        return Optional.empty();
    }

    protected T parseCallArguments(JsonNode callArguments) throws JsonProcessingException {
        return mapper.readValue(callArguments.asText(), callArgumentsClass);
    }

}
