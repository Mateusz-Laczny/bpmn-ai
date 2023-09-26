package edu.agh.bpmnai.generator.openai.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FunctionParametersProperties(String type, String description, List<String> enumValues) {

    public FunctionParametersProperties(String type, String description, List<String> enumValues) {
        this.type = type;
        this.description = description;
        if (enumValues != null) {
            this.enumValues = new ArrayList<>(enumValues);
        } else {
            this.enumValues = null;
        }
    }

    public FunctionParametersProperties(String type, String description) {
        this(type, description, null);
    }
}
