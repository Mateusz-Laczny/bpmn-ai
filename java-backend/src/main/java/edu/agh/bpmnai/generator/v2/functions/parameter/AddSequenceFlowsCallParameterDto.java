package edu.agh.bpmnai.generator.v2.functions.parameter;

import java.util.Set;

public record AddSequenceFlowsCallParameterDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("What is this action trying to achieve? Example: \"Tasks 'Preheat oven' and 'Prepare dough' can "
                     + "be done at the same time, so I will include them inside a parallel fork\"")
        String reasoning,
        @Description("Sequence flows to add to the diagram")
        Set<SequenceFlowDto> sequenceFlows
) {
}
