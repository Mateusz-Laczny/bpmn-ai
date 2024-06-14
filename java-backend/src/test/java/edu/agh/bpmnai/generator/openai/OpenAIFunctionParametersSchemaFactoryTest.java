package edu.agh.bpmnai.generator.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.v2.functions.parameter.Description;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenAIFunctionParametersSchemaFactoryTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldGenerateSchemaForSimpleDto() throws JsonProcessingException {
        record TestFunctionParametersDto(@Description("Some description 1") String someString,
                                         @Description("Some description 2") Integer someValue) {}
        JsonNode actualJsonSchema = OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(
                TestFunctionParametersDto.class);

        JsonNode expectedJsonSchema = mapper.readTree("""
                                                      {
                                                        "type": "object",
                                                        "properties": {
                                                          "someString": {
                                                            "description": "Some description 1",
                                                            "type": "string"
                                                          },
                                                          "someValue": {
                                                            "description": "Some description 2",
                                                            "type": "integer"
                                                          }
                                                        },
                                                        "required": [
                                                          "someString",
                                                          "someValue"
                                                        ]
                                                      }""");
        assertEquals(expectedJsonSchema, actualJsonSchema);
    }

    @Test
    void shouldCorrectlyGenerateSchemaForDtoWithOptionalParameters() throws JsonProcessingException {
        record TestFunctionParametersDto(@Description("Some description 1") String someString,
                                         @Description("Some description 2") @Nullable Integer someValue) {}
        JsonNode actualJsonSchema = OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(
                TestFunctionParametersDto.class);

        JsonNode expectedJsonSchema = mapper.readTree("""
                                                      {
                                                        "type": "object",
                                                        "properties": {
                                                          "someString": {
                                                            "description": "Some description 1",
                                                            "type": "string"
                                                          },
                                                          "someValue": {
                                                            "description": "Some description 2",
                                                            "type": "integer"
                                                          }
                                                        },
                                                        "required": [
                                                          "someString"
                                                        ]
                                                      }""");
        assertEquals(expectedJsonSchema, actualJsonSchema);
    }

    @Test
    void shouldCorrectlyGenerateSchemaForDtoWithEnumParameters() throws JsonProcessingException {
        enum TestEnum {FIRST_VALUE, SECOND_VALUE}
        record TestFunctionParametersDto(@Description("Some description 1") String someString,
                                         @Description("Some description 2") TestEnum someEnum) {}
        JsonNode actualJsonSchema = OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(
                TestFunctionParametersDto.class);

        JsonNode expectedJsonSchema = mapper.readTree("""
                                                      {
                                                        "type": "object",
                                                        "properties": {
                                                          "someString": {
                                                            "type": "string",
                                                            "description": "Some description 1"
                                                          },
                                                          "someEnum": {
                                                            "type": "string",
                                                            "enum": [
                                                              "FIRST_VALUE",
                                                              "SECOND_VALUE"
                                                            ],
                                                            "description": "Some description 2"
                                                          }
                                                        },
                                                        "required": [
                                                          "someString",
                                                          "someEnum"
                                                        ]
                                                      }""");
        assertEquals(expectedJsonSchema, actualJsonSchema);
    }

    @Test
    void shouldGenerateSchemaForDtoWithListParameters() throws JsonProcessingException {
        record ListElement(@Description("Some description 1") String someString,
                           @Description("Some description 2") Integer someValue) {}
        record TestFunctionParametersDto(@Description("Some description 3") List<ListElement> someArray) {}
        JsonNode actualJsonSchema = OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(
                TestFunctionParametersDto.class);

        JsonNode expectedJsonSchema = mapper.readTree("""
                                                      {
                                                        "type": "object",
                                                        "properties": {
                                                          "someArray": {
                                                            "type": "array",
                                                            "description": "Some description 3",
                                                            "items": {
                                                              "type": "object",
                                                              "properties": {
                                                                "someString": {
                                                                  "type": "string",
                                                                  "description": "Some description 1"
                                                                },
                                                                "someValue": {
                                                                  "type": "integer",
                                                                  "description": "Some description 2"
                                                                }
                                                              },
                                                              "description": "Some description 3",
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