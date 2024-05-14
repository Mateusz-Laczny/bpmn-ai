package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import jakarta.annotation.Nullable;

import java.util.List;

public record WhileLoopDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("Name of the whole subprocess.")
        String subprocessName,
        @Description("Task in which the condition is checked, that determines which activity be executed next. If"
                     + " does not exist currently in the diagram, it will be added. In the verb+object naming "
                     + "convention, should be a question like 'Pizza ok?'. Can be a name for a new element or "
                     + "name#id for an existing one. If already exists in the diagram, it will also act as an "
                     + "insertion point.")
        String checkTask,
        @Description("Diagram node after which the subprocess will be inserted.  "
                     + "Must be an element name that exists in the diagram. Must be provided, if `checkTask` does not "
                     + "yet exist in the diagram. Must have exactly 0 or 1 successors. The gateway will be inserted "
                     + "between the insertion point and it's current successor if it exists.")
        @Nullable
        HumanReadableId insertionPoint,
        @Description("Sequence of tasks executed in order inside the loop. The last task is connected via "
                     + "the sequence flow to the XOR gateway which serves as the start of the loop. In the verb+object "
                     + "naming convention.")
        List<String> tasksInLoop
) {
}
