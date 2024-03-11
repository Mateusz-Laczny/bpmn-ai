package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveElementDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class RemoveElementFunction {
    public static final String FUNCTION_NAME = "remove_element";
    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description("Removes the given element from the diagram. All activity predecessors will be connected to the activity successor")
            .parameters(getSchemaForParametersDto(RemoveElementDto.class))
            .build();
}
