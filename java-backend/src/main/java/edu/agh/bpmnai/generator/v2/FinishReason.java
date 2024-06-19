package edu.agh.bpmnai.generator.v2;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public enum FinishReason {
    OK("ok"),
    ERROR("error");

    private final String jsonSerializedValue;

    FinishReason(String jsonSerializedValue) {
        this.jsonSerializedValue = jsonSerializedValue;
    }

    @JsonSerialize
    public String getJsonSerializedValue() {
        return jsonSerializedValue;
    }
}
