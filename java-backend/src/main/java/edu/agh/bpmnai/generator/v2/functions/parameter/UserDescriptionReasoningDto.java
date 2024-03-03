package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.v2.Description;

public record UserDescriptionReasoningDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("Reasoning about the user's message in the context of the current state of the diagram. Should focus on those aspects in particular: possible missing details, paths in the model besides the happy path. At least 200 characters long.") String reasoning,
        @Description("Message to the user. If a question, should also provide a reasonable possible answer") String messageToTheUser,
        @Description("Whether more information is required to create the diagram. If false, start creating the diagram using other functions")
        boolean needMoreInfo) {
}
