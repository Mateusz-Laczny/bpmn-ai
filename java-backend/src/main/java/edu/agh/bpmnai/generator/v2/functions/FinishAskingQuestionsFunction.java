package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.FinishAskingQuestionsDto;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;

public class FinishAskingQuestionsFunction {
    public static final String FUNCTION_NAME = "finish_asking_questions";
    public static final ChatFunctionDto FUNCTION_DTO = ChatFunctionDto.builder()
            .name(FUNCTION_NAME)
            .description("Finish asking questions and go to the next step of the process.")
            .parameters(getSchemaForParametersDto(FinishAskingQuestionsDto.class)).build();
}
