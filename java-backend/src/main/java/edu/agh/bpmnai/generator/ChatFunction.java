package edu.agh.bpmnai.generator;

import java.util.Map;

public record ChatFunction(
        String name,
        String description,
        Map<String, Object> parameters
) {
}
