package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;

import java.util.List;

public record SequenceOfTasksDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("What is this action trying to achieve? Example: \"Tasks 'Select pizza' and 'Order pizza' must "
                     + "be executed in this order, so I will add them as a sequence\"")
        String reasoning,
        @Description("Diagram element, which will be the start of the added sequence in the process flow. Must be an "
                     + "element that exists in the diagram.")
        HumanReadableId startOfSequence,
        @Description("Tasks which will be added to the diagram, in the verb+object naming convention. Each "
                     + "task will be connected to the next task via a sequence flow. Last task will be "
                     + "connected to the current successor of 'startOfSequence' node.")
        List<String> tasksInSequence
) {
}
