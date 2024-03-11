package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.IfElseBranchingDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class AddIfElseBranchingFunction {
    public static final String FUNCTION_NAME = "add_if_else_branching";
    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description("Adds an if-else branching to the model. If the condition is true, one branch is executed, if not another branch will be executed")
            .parameters(getSchemaForParametersDto(IfElseBranchingDto.class))
            .build();
}
