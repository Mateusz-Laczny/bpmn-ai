package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.FillInMissingDetailsParametersDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class FillInMissingDetailsInUserRequestFunction {
    public static final String FUNCTION_NAME = "fill_in_missing_details_in_user_request";

    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder().name(FUNCTION_NAME).description(
            "Think about and provide an updated request, filled in with missing details, focus especially on "
            + "potential " + "problems and alternative paths.").parameters(getSchemaForParametersDto(
            FillInMissingDetailsParametersDto.class)).build();
}
