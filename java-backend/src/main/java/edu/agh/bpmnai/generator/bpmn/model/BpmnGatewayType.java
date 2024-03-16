package edu.agh.bpmnai.generator.bpmn.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BpmnGatewayType {
    EXCLUSIVE("exclusive"),
    PARALLEL("inclusive");

    private final String jsonValue;

    BpmnGatewayType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String getJsonValue() {
        return jsonValue;
    }
}
