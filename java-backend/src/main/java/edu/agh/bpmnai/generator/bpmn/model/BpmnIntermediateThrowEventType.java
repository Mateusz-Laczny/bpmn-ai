package edu.agh.bpmnai.generator.bpmn.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BpmnIntermediateThrowEventType {
    EMPTY("empty"),
    MESSAGE("message"),
    ESCALATION("escalation"),
    LINK("link"),
    COMPENSATION("compensation"),
    SIGNAL("signal");

    private final String jsonValue;

    BpmnIntermediateThrowEventType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String getJsonValue() {
        return jsonValue;
    }
}
