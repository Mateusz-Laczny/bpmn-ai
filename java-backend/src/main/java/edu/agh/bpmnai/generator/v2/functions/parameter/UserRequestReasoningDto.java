package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.v2.Description;

public record UserRequestReasoningDto(
        @Description("Reasoning about the user's message in the context of the current state of the diagram. Should focus on those aspects in particular: possible missing details, paths in the model besides the happy path. At least 200 characters long.") String reasoning,
        @Description("Message to the user. If a question, should also provide a reasonable possible answer") String messageToTheUser,
        @Description("Whether it is required to ask more questions to the user.")
        boolean askMoreQuestions) {
}
