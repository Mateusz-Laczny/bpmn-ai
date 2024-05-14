package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import jakarta.annotation.Nullable;

public record IfElseBranchingDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("What is this action trying to achieve? Example: \"I need to include the choice between ordering"
                     + " a pizza and a burger, so I will add an if-else branching with tasks 'Pizza ordered' and "
                     + "'Burger ordered'\"")
        String reasoning,
        @Description("Name for the whole element")
        String elementName,
        @Description("Task in which the condition is checked, that determines which path will be executed. Should be "
                     + "a name for a new element or name#id for existing elements. In the verb+object naming "
                     + "convention.")
        String checkTask,
        @Description("Diagram element, which is the direct predecessor to the added while loop in the process flow. "
                     + "Must be an element that exists in the diagram. If the `checkTask` is a task that already "
                     + "exists in the model, this parameter will be ignored.")
        @Nullable
        HumanReadableId predecessorElement,
        @Description("Task that is the beginning of the true branch. May or may not exist in the diagram.")
        Task trueBranchBeginningTask,
        @Description("Task that is the beginning of the false branch. May or may not exist in the diagram.")
        Task falseBranchBeginningTask
) {
}
