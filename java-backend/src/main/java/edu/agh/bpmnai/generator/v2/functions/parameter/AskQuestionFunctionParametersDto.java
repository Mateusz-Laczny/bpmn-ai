package edu.agh.bpmnai.generator.v2.functions.parameter;

public record AskQuestionFunctionParametersDto(
        @Description("Reasoning about the user's message in the context of the current state of the diagram. Should focus on those aspects in particular: possible missing details, paths in the model besides the happy path. At least 200 characters long.") String reasoning,
        @Description("Question to the user, should also provide a reasonable possible answer") String messageToTheUser) {
}
