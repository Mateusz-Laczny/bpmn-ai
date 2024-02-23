package edu.agh.bpmnai.generator.v2.functions;

import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public record FunctionCallResult(List<String> errors, Map<String, String> additionalData,
                                 @Nullable String messageToUser) {

    public static FunctionCallResult successfulCall() {
        return new FunctionCallResult(emptyList(), emptyMap(), null);
    }

    public static FunctionCallResult unsuccessfulCall(List<String> errors) {
        return new FunctionCallResult(List.copyOf(errors), emptyMap(), null);
    }

    public static FunctionCallResult withMessageToUser(String messageToUser) {
        return new FunctionCallResult(emptyList(), emptyMap(), messageToUser);
    }

    public boolean successful() {
        return errors.isEmpty();
    }
}
