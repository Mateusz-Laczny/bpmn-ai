package edu.agh.bpmnai.generator.openai.model;

public record ErrorProperties(String message, String type, String param, String code) {
}
