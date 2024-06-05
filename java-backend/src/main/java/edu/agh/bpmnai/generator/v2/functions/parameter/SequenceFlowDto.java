package edu.agh.bpmnai.generator.v2.functions.parameter;

public record SequenceFlowDto(
        @Description("Source node of the sequence flow")
        String source,
        @Description("Target node of the sequence flow")
        String target) {
}
