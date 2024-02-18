package edu.agh.bpmnai.generator.v2;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

public class MapElementsToFieldsSerializer extends JsonSerializer<Map<String, String>> {

    @Override
    public void serialize(Map<String, String> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            jsonGenerator.writeStringField("key", entry.getKey());
            jsonGenerator.writeStringField("value", entry.getValue());
        }
        jsonGenerator.writeEndObject();
    }
}

