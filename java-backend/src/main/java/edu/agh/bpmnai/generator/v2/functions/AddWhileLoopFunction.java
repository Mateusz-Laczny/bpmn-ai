package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.WhileLoopDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class AddWhileLoopFunction {
    public static final String FUNCTION_NAME = "add_while_loop";
    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description("Adds a while loop to the model, where one or more tasks can be executed multiple times, based on a condition")
            .parameters(getSchemaForParametersDto(WhileLoopDto.class))
            .build();
}
