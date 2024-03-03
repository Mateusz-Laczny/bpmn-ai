package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.v2.Description;
import jakarta.annotation.Nullable;

import java.util.List;

public record SingleChoiceForkDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("What is this action trying to achieve? Example: \"The customer can choose which mode of transport they prefer. There are 4 possible modes of transport, so I will add a single choice fork, which includes those choices\"")
        String reasoning,
        @Description("Name of the element")
        String elementName,
        @Description("Task in which the condition is checked, that determines which task will be executed next. Does not have to exist in the diagram. In the verb+object naming convention")
        String checkTask,
        @Description("Model element, which will be the direct predecessor to added fork in the process flow. Must be an element name that exists in the diagram, or a special 'Start' element, indicating the start of the process. Must be provided, if `checkTask` does not yet exist in the diagram")
        @Nullable
        String predecessorElement,
        @Description("Tasks ot choose from. The chosen task will be executed next in the process. In the verb+object naming convention.")
        List<String> tasksToChooseFrom
) {
}
