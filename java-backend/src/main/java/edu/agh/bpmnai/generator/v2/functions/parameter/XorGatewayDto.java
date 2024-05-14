package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import jakarta.annotation.Nullable;

import java.util.List;

public record XorGatewayDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("What is this action trying to achieve? Example: \"The customer can choose which mode of "
                     + "transport they prefer. There are 4 possible modes of transport, so I will add a single choice"
                     + " fork, which includes those choices\"")
        String reasoning,
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
        @Description("Tasks, which will be added inside the gateway. In the verb+object naming convention. For "
                     + "the gateway to make sense it must contain at least 2 activities.")
        List<Task> tasksInsideGateway
) {
}
