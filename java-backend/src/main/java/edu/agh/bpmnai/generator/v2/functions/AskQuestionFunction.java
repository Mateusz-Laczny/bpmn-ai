package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.AskQuestionFunctionParametersDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class AskQuestionFunction {

    public static final String FUNCTION_NAME = "ask_user_a_question";

    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description("Checks if the user's request is detailed enough and asks for more details only if necessary.")
            .parameters(getSchemaForParametersDto(AskQuestionFunctionParametersDto.class)).build();
}
