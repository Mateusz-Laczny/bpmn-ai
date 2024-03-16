package edu.agh.bpmnai.generator.v2.functions.parameter;

import jakarta.annotation.Nullable;

import java.util.List;

public record WhileLoopDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("Name of the whole element")
        String elementName,
        @Description("Task in which the condition is checked, that determines which task will be executed next. Does not have to exist in the diagram. In the verb+object naming convention, should be a question like 'Pizza ok?'")
        String checkTask,
        @Description("Diagram element, which will be the direct predecessor to the added while loop in the process flow. Must be an element name that exists in the diagram, or a special 'Start' element. If the `checkActivity` already exists in the model, this parameter will be ignored.")
        @Nullable
        String predecessorElement,
        @Description("Sequence of activities executed in order inside the loop. The last activity is connected via the sequence flow to the start of the loop. In the verb+object naming convention.")
        List<String> activitiesInLoop
) {
}
