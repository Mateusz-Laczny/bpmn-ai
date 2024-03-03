package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.v2.Description;
import jakarta.annotation.Nullable;

public record IfElseBranchingDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("What is this action trying to achieve? Example: \"I need to include the choice between ordering a pizza and a burger, so I will add an if-else branching with tasks 'Pizza ordered' and 'Burger ordered'\"")
        String reasoning,
        @Description("Task in which the condition is checked, that determines which path will be executed. Does not have to exist in the diagram. In the verb+object naming convention")
        String checkTask,
        @Description("Diagram element, which is the direct predecessor to the added while loop in the process flow. Must be an element that exists in the diagram, or a special 'Start' element. If the `checkTask` is a task that already exists in the model, this parameter will be ignored.")
        @Nullable
        String predecessorElement,
        @Description("Task that is the beginning of the true branch. May or may not exist in the diagram.")
        String trueBranchBeginningTask,
        @Description("Task that is the beginning of the false branch. May or may not exist in the diagram.")
        String falseBranchBeginningTask
) {
}
