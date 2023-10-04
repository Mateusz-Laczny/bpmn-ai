package edu.agh.bpmnai.generator.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenAIFunctionParametersSchemaFactoryTest {

    private final ObjectMapper mapper = new ObjectMapper();

    OpenAIFunctionParametersSchemaFactory schemaGenerator;

    @BeforeEach
    void setUp() {
        schemaGenerator = new OpenAIFunctionParametersSchemaFactory();
    }

    @Test
    void shouldGenerateSchemaForSimpleDto() throws JsonProcessingException {
        record TestFunctionParametersDto(String someString, Integer someValue) {
        }
        JsonNode actualJsonSchema = OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(TestFunctionParametersDto.class);

        JsonNode expectedJsonSchema = mapper.readTree("""
                {
                    "type": "object",
                    "properties": {
                        "someString": {
                            "type": "string"
                        },
                        "someValue": {
                            "type": "integer"
                        }
                    },
                    "required": ["someString", "someValue"]
                }""");
        assertEquals(expectedJsonSchema, actualJsonSchema);
    }

    @Test
    void shouldCorrectlyGenerateSchemaForDtoWithOptionalParameters() throws JsonProcessingException {
        record TestFunctionParametersDto(String someString, @Nullable Integer someValue) {
        }
        JsonNode actualJsonSchema = OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(TestFunctionParametersDto.class);

        JsonNode expectedJsonSchema = mapper.readTree("""
                {
                    "type": "object",
                    "properties": {
                        "someString": {
                            "type": "string"
                        },
                        "someValue": {
                            "type": "integer"
                        }
                    },
                    "required": ["someString"]
                }""");
        assertEquals(expectedJsonSchema, actualJsonSchema);
    }

    @Test
    void shouldCorrectlyGenerateSchemaForDtoWithEnumParameters() throws JsonProcessingException {
        enum TestEnum {FIRST_VALUE, SECOND_VALUE}
        record TestFunctionParametersDto(String someString, TestEnum someEnum) {
        }
        JsonNode actualJsonSchema = OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(TestFunctionParametersDto.class);

        JsonNode expectedJsonSchema = mapper.readTree("""
                {
                    "type": "object",
                    "properties": {
                        "someString": {
                            "type": "string"
                        },
                        "someEnum": {
                            "type": "string",
                            "enum": ["FIRST_VALUE", "SECOND_VALUE"]
                        }
                    },
                    "required": ["someString", "someEnum"]
                }""");
        assertEquals(expectedJsonSchema, actualJsonSchema);
    }

    @Test
    void shouldGenerateSchemaForDtoWithListParameters() throws JsonProcessingException {
        record ListElement(String someString, Integer someValue) {
        }
        record TestFunctionParametersDto(List<ListElement> someArray) {
        }
        JsonNode actualJsonSchema = OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(TestFunctionParametersDto.class);

        JsonNode expectedJsonSchema = mapper.readTree("""
                {
                  "type": "object",
                  "properties": {
                    "someArray": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "properties": {
                          "someString": {
                            "type": "string"
                          },
                          "someValue": {
                            "type": "integer"
                          }
                        },
                        "required": [
                          "someString",
                          "someValue"
                        ]
                      }
                    }
                  },
                  "required": [
                    "someArray"
                  ]
                }""");
        assertEquals(expectedJsonSchema, actualJsonSchema);
    }
}