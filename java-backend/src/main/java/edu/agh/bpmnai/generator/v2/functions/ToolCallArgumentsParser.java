package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.parameter.NullabilityCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class ToolCallArgumentsParser {

    private final ObjectMapper objectMapper;

    private final NullabilityCheck checkFieldsForNull;

    @Autowired
    public ToolCallArgumentsParser(ObjectMapper objectMapper, NullabilityCheck checkFieldsForNull) {
        this.objectMapper = objectMapper;
        this.checkFieldsForNull = checkFieldsForNull;
    }

    public <T> Result<T, String> parseArguments(String argumentsJson, Class<T> targetClass) {
        T parsedValue;
        try {
            parsedValue = objectMapper.readValue(argumentsJson, targetClass);
        } catch (JsonProcessingException e) {
            log.info("Failed parsing call arguments", e);
            return Result.error("The tool call arguments could not be parsed: '%s'".formatted(e.getMessage()));
        }

        Set<String> nullFieldsNotMarkedAsNullable = checkFieldsForNull.check(parsedValue);
        if (!nullFieldsNotMarkedAsNullable.isEmpty()) {
            return Result.error("Following required parameters are missing: %s".formatted(nullFieldsNotMarkedAsNullable));
        }

        return Result.ok(parsedValue);
    }
}
