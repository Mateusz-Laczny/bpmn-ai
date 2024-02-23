package edu.agh.bpmnai.generator.openai;

import edu.agh.bpmnai.generator.openai.model.ChatFunction;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import edu.agh.bpmnai.generator.openai.model.FunctionCallDto;

import java.util.*;

public class ChatCallableInterface {
    private final Set<ChatFunction> callableFunctions;

    private final Map<String, ChatFunction> nameToFunctionMap;

    public ChatCallableInterface(Set<ChatFunction> callableFunctions) {
        this.callableFunctions = new HashSet<>(callableFunctions);
        this.nameToFunctionMap = new HashMap<>();
        for (ChatFunction function : callableFunctions) {
            nameToFunctionMap.put(function.name(), function);
        }
    }

    public Set<ChatFunction> getCallableFunctions() {
        return Collections.unmodifiableSet(callableFunctions);
    }

    public Optional<ChatMessage> executeFunctionCall(FunctionCallDto functionCall) {
        if (!nameToFunctionMap.containsKey(functionCall.name())) {
            return Optional.of(ChatMessage.userMessage("The function \"" + functionCall.name() + "\n does not exist."));
        }

        ChatFunction calledFunction = nameToFunctionMap.get(functionCall.name());
        return calledFunction.executor().apply(functionCall.argumentsJson());
    }
}
