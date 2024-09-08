package edu.agh.bpmnai.generator;

public record NewSessionRequest(String apiKey) {
    @Override
    public String toString() {
        return "NewSessionRequest{}";
    }
}
