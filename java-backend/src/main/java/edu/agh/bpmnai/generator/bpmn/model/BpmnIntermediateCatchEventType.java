package edu.agh.bpmnai.generator.bpmn.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BpmnIntermediateCatchEventType {
    MESSAGE("message"),
    TIMER("timer"),
    CONDITIONAL("conditional"),
    LINK("link"),
    SIGNAL("signal");

    private final String jsonValue;

    BpmnIntermediateCatchEventType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String getJsonValue() {
        return jsonValue;
    }
}
