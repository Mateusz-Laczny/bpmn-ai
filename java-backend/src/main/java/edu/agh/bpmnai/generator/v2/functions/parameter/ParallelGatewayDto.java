package edu.agh.bpmnai.generator.v2.functions.parameter;

import java.util.List;

public record ParallelGatewayDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("What is this action trying to achieve? Example: \"Tasks 'Preheat oven' and 'Prepare dough' can be done at the same time, so I will include them inside a parallel fork\"")
        String reasoning,
        @Description("Name of the whole fork")
        String elementName,
        @Description("Model element, which is the direct predecessor to added gateway in the process flow. Must be an element that was previously added to the diagram, or a special 'Start' activity indicating the start of the process.")
        String predecessorElement,
        @Description("Tasks to execute, possibly in parallel. In the verb+object naming convention. For the parallel gateway to make sense, it must contain at least 2 tasks")
        List<String> tasksToExecute
) {
}
