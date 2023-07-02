package edu.agh.bpmnai.generator.openai.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageRole {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
    FUNCTION("function");

    private final String roleName;

    MessageRole(String roleName) {
        this.roleName = roleName;
    }

    @JsonValue
    public String getRoleName() {
        return roleName;
    }
}
