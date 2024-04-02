package edu.agh.bpmnai.generator.v2.functions.parameter;

import java.util.Set;

public record AddSequenceFlowsCallParameterDto(
        @Description("Sequence flows to add to the diagram")
        Set<SequenceFlowDto> sequenceFlows
) {
}
