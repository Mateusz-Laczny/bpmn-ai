package edu.agh.bpmnai.generator.openai;

public class FailedRequestException extends RuntimeException {
    public FailedRequestException(Throwable cause) {
        super("Request failed with exception", cause);
    }

    public FailedRequestException(String message) {
        super(message);
    }
}
