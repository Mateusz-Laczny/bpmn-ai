package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.v2.Description;

public record RemoveActivityDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("What is this action trying to achieve? Example: \"I need to remove the activity 'Perform quality checks', since the user said that they don;t perform any checks\"")
        String reasoning,
        @Description("Element to remove from the diagram. Must exist in the diagram.")
        String elementToRemove
) {
}
