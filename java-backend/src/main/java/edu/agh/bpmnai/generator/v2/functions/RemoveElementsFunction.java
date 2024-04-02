package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveElementDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class RemoveElementsFunction {
    public static final String FUNCTION_NAME = "remove_elements";
    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description("Removes the given element from the "
                         + "diagram. All incoming and outgoing sequence flows will be removed")
            .parameters(getSchemaForParametersDto(RemoveElementDto.class))
            .build();
}
