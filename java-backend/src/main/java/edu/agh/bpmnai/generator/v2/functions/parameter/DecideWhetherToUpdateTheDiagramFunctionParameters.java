package edu.agh.bpmnai.generator.v2.functions.parameter;

public record DecideWhetherToUpdateTheDiagramFunctionParameters(
        @Description("Step by step reasoning about the user's request")
        String reasoning,
        @Description("Final message to the user. Ignored, if 'diagramNeedsUpdate' is set to true")
        String finalMessage,
        @Description("Whether the diagram needs updating")
        boolean diagramNeedsUpdate
) {
}
