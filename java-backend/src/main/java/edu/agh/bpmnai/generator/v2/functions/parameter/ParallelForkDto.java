package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.v2.Description;

import java.util.List;

public record ParallelForkDto(
        @Description("Retrospective summary of the current state of the model")
        RetrospectiveSummary retrospectiveSummary,
        @Description("Name of the whole fork")
        String elementName,
        @Description("Model element, which is the direct predecessor to added fork in the process flow. Must be an element that was previously added to the model, or a special 'Start' activity indicating the start of the process.")
        String predecessorElement,
        @Description("List of activities to execute, possibly in parallel. In the verb+object naming convention. For the parallel fork to make sense, it must contain at least 2 activities")
        List<String> activitiesToExecute
) {
}
