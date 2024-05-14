package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;

import java.util.List;

public record ParallelGatewayDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("What is this action trying to achieve? Example: \"Tasks 'Preheat oven' and 'Prepare dough' can "
                     + "be done at the same time, so I will include them inside a parallel fork\"")
        String reasoning,
        @Description("Name of the whole subprocess.")
        String subprocessName,
        @Description("Diagram node after which the subprocess will be inserted. Must "
                     + "exist in the model and have exactly 0 or one successors. The subprocess will be inserted "
                     + "between"
                     + " the insertion point and it's successor.")
        HumanReadableId insertionPoint,
        @Description("Tasks inside the gateway. In the verb+object naming convention. A gateway must contain at least"
                     + " 2 activities")
        List<Task> tasksInsideGateway
) {
}
