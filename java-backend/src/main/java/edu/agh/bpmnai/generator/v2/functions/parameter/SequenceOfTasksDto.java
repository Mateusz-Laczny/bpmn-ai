package edu.agh.bpmnai.generator.v2.functions.parameter;

import java.util.List;

public record SequenceOfTasksDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("What is this action trying to achieve? Example: \"Tasks 'Select pizza' and 'Order pizza' must "
                     + "be executed in this order, so I will add them as a sequence\"")
        String reasoning,
        @Description("Diagram node after which the subprocess will be inserted.  "
                     + "Must be a node id which exists in the diagram. Must have exactly 0 or 1 successors.")
        String insertionPoint,
        @Description("Tasks which will be added to the diagram, in the verb+object naming convention. Each "
                     + "task will be connected to the next task via a sequence flow. Last task will be "
                     + "connected to the current successor of the insertion point node, if it exists.")
        List<String> tasksInSequence
) {
}
