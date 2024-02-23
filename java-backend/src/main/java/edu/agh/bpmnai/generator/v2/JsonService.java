package edu.agh.bpmnai.generator.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JsonService {

    private final ObjectMapper objectMapper;

    @Autowired
    public JsonService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T jsonNodeToObject(JsonNode jsonNode, Class<T> targetClass) {
        try {
            return objectMapper.readValue(jsonNode.asText(), targetClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
