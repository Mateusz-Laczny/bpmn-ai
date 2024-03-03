package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.v2.Description;

import java.util.List;

public record SequenceOfTasksDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("What is this action trying to achieve? Example: \"Tasks 'Select pizza' and 'Order pizza' must be executed in this order, so I will add them as a sequence\"")
        String reasoning,
        @Description("Diagram element, which will be the direct predecessor to added sequence in the process flow. Must be an element that exists in the diagram, or a special 'Start' element.")
        String predecessorElement,
        @Description("Tasks to execute one after another, in the verb+object naming convention.")
        List<String> tasksInSequence
) {
}
