package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveElementsFunctionCallDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class RemoveSequenceFlowsFunction {
    public static final String FUNCTION_NAME = "remove_sequence_flows";
    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description("Removes the given sequence flows from the " + "diagram. All activity predecessors "
                         + "will be connected to the activity " + "successor")
            .parameters(getSchemaForParametersDto(RemoveElementsFunctionCallDto.class))
            .build();
}
