package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.RespondWithoutModifyingDiagramParametersDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class RespondWithoutModifyingDiagramFunction {
    public static final String FUNCTION_NAME = "respond_without_modifying_model";

    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description("Sends a message to the user without modifying the model")
            .parameters(getSchemaForParametersDto(RespondWithoutModifyingDiagramParametersDto.class))
            .build();
}
