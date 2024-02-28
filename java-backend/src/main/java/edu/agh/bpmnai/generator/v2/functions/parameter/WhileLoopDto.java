package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.v2.Description;
import jakarta.annotation.Nullable;

import java.util.List;

public record WhileLoopDto(
        @Description("Background for the specific update, and how it addresses the problem specified by the user")
        String background,
        @Description("Activity in which the condition is checked, that determines whether the next iteration of the loop will be executed. Does not have to exist in the model. In the verb+object naming convention")
        String checkActivity,
        @Description("Model element, which will be the direct predecessor to the added while loop in the process flow. Must be an element name that exists in the model, or a special 'Start' or 'End activity. If the `checkActivity` is an activity that already exists in the model, this parameter should not be used.")
        @Nullable
        String predecessorElement,
        @Description("Sequence of activities executed in order inside the loop. In the verb+object naming convention.")
        List<String> activitiesInLoop
) {
}
