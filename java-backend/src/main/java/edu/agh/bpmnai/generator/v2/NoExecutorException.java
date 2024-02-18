package edu.agh.bpmnai.generator.v2;

public class NoExecutorException extends Exception {
    public NoExecutorException(String functionName) {
        super("No executor defined for function '%s'".formatted(functionName));
    }
}
