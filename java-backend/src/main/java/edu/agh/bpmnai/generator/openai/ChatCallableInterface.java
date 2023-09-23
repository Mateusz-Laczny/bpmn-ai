package edu.agh.bpmnai.generator.openai;

import edu.agh.bpmnai.generator.openai.model.ChatFunction;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChatCallableInterface {
    private final Set<ChatFunction> callableFunctions;

    public ChatCallableInterface(Set<ChatFunction> callableFunctions) {
        this.callableFunctions = new HashSet<>(callableFunctions);
    }

    public Set<ChatFunction> getCallableFunctions() {
        return Collections.unmodifiableSet(callableFunctions);
    }
}
