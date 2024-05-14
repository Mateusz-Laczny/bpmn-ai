package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.ParallelGatewayDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class AddParallelGatewayFunction {
    public static final String FUNCTION_NAME = "add_parallel_gateway_subprocess";
    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description(
                    "Adds a gateway subprocess to the model, where two or more activities have to be executed, which "
                    + "can be executed at the same time. After the gateway, the paths converge on a single point, "
                    + "from which the process is continued. The subprocess starts and ends with a parallel gateway")
            .parameters(getSchemaForParametersDto(ParallelGatewayDto.class))
            .build();
}
