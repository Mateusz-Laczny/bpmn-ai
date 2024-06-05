package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.AddSequenceFlowsCallParameterDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class AddSequenceFlowsFunction {
    public static String FUNCTION_NAME = "add_sequence_flows";

    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description("Adds sequence flows to the diagram.")
            .parameters(getSchemaForParametersDto(AddSequenceFlowsCallParameterDto.class))
            .build();
}
