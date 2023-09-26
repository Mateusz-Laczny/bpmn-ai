package edu.agh.bpmnai.generator.openai.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@JsonSerialize(using = FunctionParameters.FunctionParametersSerializer.class)
public class FunctionParameters {
    private Map<String, FunctionParametersProperties> nameToParameterPropertiesMap;

    private List<String> requiredParametersNames;

    public FunctionParameters() {
        this.nameToParameterPropertiesMap = new HashMap<>();
        requiredParametersNames = new ArrayList<>();
    }

    public FunctionParameters addRequiredArgument(String name, String type, String description) {
        nameToParameterPropertiesMap.put(name, new FunctionParametersProperties(type, description));
        requiredParametersNames.add(name);
        return this;
    }

    public FunctionParameters addRequiredArgument(String name, String type, String description, List<String> enumValues) {
        nameToParameterPropertiesMap.put(name, new FunctionParametersProperties(type, description, enumValues));
        requiredParametersNames.add(name);
        return this;
    }

    public FunctionParameters addOptionalArgument(String name, String type, String description) {
        nameToParameterPropertiesMap.put(name, new FunctionParametersProperties(type, description));
        return this;
    }

    public FunctionParameters addOptionalArgument(String name, String type, String description, List<String> enumValues) {
        nameToParameterPropertiesMap.put(name, new FunctionParametersProperties(type, description, enumValues));
        return this;
    }

    static class FunctionParametersSerializer extends JsonSerializer<FunctionParameters> {
        @Override
        public void serialize(FunctionParameters functionParameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();

            jsonGenerator.writeStringField("type", "object");

            jsonGenerator.writeObjectFieldStart("properties");
            for (Map.Entry<String, FunctionParametersProperties> entry : functionParameters.nameToParameterPropertiesMap.entrySet()) {
                jsonGenerator.writePOJOField(entry.getKey(), entry.getValue());
            }
            jsonGenerator.writeEndObject();

            jsonGenerator.writePOJOField("required", functionParameters.requiredParametersNames);

            jsonGenerator.writeEndObject();
        }
    }
}
