package edu.agh.bpmnai.generator.v2.functions.parameter;

public record SequenceFlowDto(
        @Description("Source element of the sequence flow")
        String source,
        @Description("Target element of the sequence flow")
        String target) {
}
