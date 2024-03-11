package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.ParallelGatewayDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class AddParallelGatewayFunction {
    public static final String FUNCTION_NAME = "add_parallel_gateway";
    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description("Adds a gateway to the model, where two or more activities have to be executed, which can be executed at the same time. After the gateway, the paths converge on a single point, from which the process is continued.")
            .parameters(getSchemaForParametersDto(ParallelGatewayDto.class))
            .build();
}
