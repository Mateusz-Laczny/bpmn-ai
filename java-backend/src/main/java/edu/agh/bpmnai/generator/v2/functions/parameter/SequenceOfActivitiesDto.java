package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.v2.Description;

import java.util.List;

public record SequenceOfActivitiesDto(
        @Description("Background of the specific addition, and how it addresses the problem specified by the user")
        String background,
        @Description("Model element, which will be the direct predecessor to added sequence in the process flow. Must be an element that exists in the model, or a special 'Start' activity.")
        String predecessorElement,
        @Description("List of activities that will be added to the model, in the verb+object naming convention.")
        List<String> newActivities
) {
}
