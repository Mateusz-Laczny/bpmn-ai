package edu.agh.bpmnai.generator.v2.functions.parameter;

import java.util.List;

public record SequenceOfTasksDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("What is this action trying to achieve? Example: \"Tasks 'Select pizza' and 'Order pizza' must be executed in this order, so I will add them as a sequence\"")
        String reasoning,
        @Description("Diagram element, which will be the start of the added sequence in the process flow. Must be an element that exists in the diagram, or a special 'Start' element.")
        String startOfSequence,
        @Description("Activities which will be added to the diagram, in the verb+object naming convention. Each activity will be connected to the next activity via a sequence flow")
        List<Activity> activitiesInSequence
) {
}
