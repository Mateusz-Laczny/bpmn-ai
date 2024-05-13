package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.RespondWithoutModifyingDiagramParametersDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class RespondWithoutModifyingDiagramFunction {
    public static final String FUNCTION_NAME = "user_request_does_not_require_modifying_the_diagram";

    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder().name(FUNCTION_NAME).description(
            "Sends a message to the user, and stop " + "without modifying the diagram. Use "
            + "only if the user's request does not " + "require modifying the diagram, in "
            + "other cases call the second provided" + " function.").parameters(getSchemaForParametersDto(
            RespondWithoutModifyingDiagramParametersDto.class)).build();
}
