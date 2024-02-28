package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.v2.Description;
import jakarta.annotation.Nullable;

public record IfElseBranchingDto(
        @Description("Retrospective summary of the current state of the model")
        RetrospectiveSummary retrospectiveSummary,
        @Description("Activity in which the condition is checked, that determines which path will be executed. Does not have to exist in the model. In the verb+object naming convention")
        String checkActivity,
        @Description("Model element, which is the direct predecessor to the added while loop in the process flow. Must be an element that exists in the model, or a special 'Start' or 'End activity. If the `checkActivity` is an activity that already exists in the model, this parameter should not be used.")
        @Nullable
        String predecessorElement,
        @Description("Activity that is the beginning of the true branch. May or may not exist in the model.")
        String trueBranchBeginningActivity,
        @Description("Activity that is the beginning of the false branch. May or may not exist in the model.")
        String falseBranchBeginningActivity
) {
}
