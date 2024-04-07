package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.datatype.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ToolCallArgumentsParser {

    private final ObjectMapper objectMapper;

    @Autowired
    public ToolCallArgumentsParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> Result<T, String> parseArguments(String argumentsJson, Class<T> targetClass) {
        try {
            T parsedValue = objectMapper.readValue(argumentsJson, targetClass);
            return Result.ok(parsedValue);
        } catch (JsonProcessingException e) {
            log.info("Failed parsing call arguments", e);
            return Result.error("The tool call arguments could not be parsed: '%s'".formatted(e.getMessage()));
        }
    }
}
