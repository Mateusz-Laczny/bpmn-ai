package edu.agh.bpmnai.generator.v2.functions.parameter;

public record SequenceFlowDto(
        @Description("Source node of the sequence flow. Must be in the name#id format, as provided in the list of "
                     + "nodes in the request context")
        String source,
        @Description("Target node of the sequence flow. Must be in the name#id format, as provided in the list of "
                     + "nodes in the request context")
        String target) {
}
