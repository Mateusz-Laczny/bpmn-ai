package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.DecideWhetherToUpdateTheDiagramFunctionParameters;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class DecideWhetherToUpdateTheDiagramFunction {
    public static final String FUNCTION_NAME = "decide_whether_to_update_the_diagram";

    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder().name(FUNCTION_NAME).description(
            "Reason about the user's request and decide whether it requires updating the diagram").parameters(
            getSchemaForParametersDto(
                    DecideWhetherToUpdateTheDiagramFunctionParameters.class)).build();
}
