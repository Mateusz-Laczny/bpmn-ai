package edu.agh.bpmnai.generator.v2;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

public class MapElementsToFieldsSerializer extends JsonSerializer<Map<String, Object>> {

    @Override
    public void serialize(Map<String, Object> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        for (Entry<String, Object> entry : map.entrySet()) {
            jsonGenerator.writeStringField(entry.getKey(), entry.getValue().toString());
        }
        jsonGenerator.writeEndObject();
    }
}

