package edu.agh.bpmnai.generator.openai.model;

import java.util.Map;

public record ChatFunction(
        String name,
        String description,
        Map<String, Object> parameters
) {
}
