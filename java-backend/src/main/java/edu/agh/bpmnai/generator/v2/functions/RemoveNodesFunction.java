package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveNodesFunctionCallDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class RemoveNodesFunction {
    public static final String FUNCTION_NAME = "remove_nodes";
    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description(
                    "Removes the provided nodes from the "
                    + "diagram. All incoming and outgoing "
                    + "sequence flows of those nodes will be removed.")
            .parameters(getSchemaForParametersDto(
                    RemoveNodesFunctionCallDto.class))
            .build();
}
