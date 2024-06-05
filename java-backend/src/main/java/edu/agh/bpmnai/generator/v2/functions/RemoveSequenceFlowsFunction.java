package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveSequenceFlowsCallParameterDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class RemoveSequenceFlowsFunction {
    public static final String FUNCTION_NAME = "remove_sequence_flows";
    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description("Removes the given sequence flows from the diagram.")
            .parameters(getSchemaForParametersDto(RemoveSequenceFlowsCallParameterDto.class))
            .build();
}
