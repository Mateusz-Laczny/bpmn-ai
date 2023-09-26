package edu.agh.bpmnai.generator.openai.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public interface FunctionCallExecutor {
    Optional<ChatMessage> executeFunction(String name, JsonNode parameters);
}
