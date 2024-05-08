package edu.agh.bpmnai.generator.bpmn.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BpmnNodeType {
    TASK("task"), OTHER_ELEMENT("other"), START_EVENT("start-event"), END_EVENT("end-event"), XOR_GATEWAY(
            "xor"), PARALLEL_GATEWAY("parallel");


    private final String stringRepresentation;

    BpmnNodeType(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    @JsonValue
    public String asString() {
        return stringRepresentation;
    }
}
