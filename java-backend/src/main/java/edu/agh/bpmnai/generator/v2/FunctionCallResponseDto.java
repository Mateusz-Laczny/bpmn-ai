package edu.agh.bpmnai.generator.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

public record FunctionCallResponseDto(@JsonProperty("call_successful") boolean callsSuccessful,
                                      @JsonInclude(JsonInclude.Include.NON_EMPTY)
                                      @JsonSerialize(using = MapElementsToFieldsSerializer.class)
                                      Map<String, String> additionalFields) {
    public FunctionCallResponseDto(boolean callsSuccessful) {
        this(callsSuccessful, Map.of());
    }
}
