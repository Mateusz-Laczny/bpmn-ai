package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.SequenceOfTasksDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class AddSequenceOfTasksFunction {
    public static final String FUNCTION_NAME = "add_sequence_of_activities";
    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description("Adds a sequence of tasks to the model, executed in a linear fashion (one after the other).")
            .parameters(getSchemaForParametersDto(SequenceOfTasksDto.class))
            .build();
}
