package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.XorGatewayDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class AddXorGatewayFunction {
    public static final String FUNCTION_NAME = "add_xor_gateway_subprocess";
    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description(
                    "Adds a xor gateway subprocess to the diagram, where one path has to be chosen from several "
                    + "alternatives. After the gateway, the paths converge on a single point, from which the process "
                    + "is continued.")
            .parameters(getSchemaForParametersDto(XorGatewayDto.class))
            .build();
}
