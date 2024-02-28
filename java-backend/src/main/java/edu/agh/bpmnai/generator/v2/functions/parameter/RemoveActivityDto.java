package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.v2.Description;

public record RemoveActivityDto(
        @Description("Retrospective summary of the current state of the model")
        RetrospectiveSummary retrospectiveSummary,
        @Description("Activity to remove from the model. Must exist in the model.")
        String activityToRemove
) {
}
