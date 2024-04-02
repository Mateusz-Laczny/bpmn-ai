package edu.agh.bpmnai.generator.v2.functions.parameter;

import java.util.List;

public record RemoveSequenceFlowsCallParameterDto(
        @Description("List of sequence flows to remove. Must exist in the diagram") List<SequenceFlowDto> sequenceFlowsToRemove) {
}
