package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.WhileLoopDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class AddWhileLoopFunction {
    public static final String FUNCTION_NAME = "add_while_loop_subprocess";
    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description(
                    "Adds a while loop subprocess to the diagram, where one or more tasks can be executed multiple "
                    + "times, based "
                    + "on a condition represented as an XOR gateway.")
            .parameters(getSchemaForParametersDto(WhileLoopDto.class))
            .build();
}
