package edu.agh.bpmnai.generator.v2.functions.parameter;

import java.util.List;

public record RemoveElementsFunctionCallDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("What is this action trying to achieve? Example: \"I need to remove the activity 'Perform "
                     + "quality checks', since the user said that they don't perform any checks\"")
        String reasoning,
        @Description("Nodes to remove from the diagram. Must exist in the diagram.") List<String> nodesToRemove
) {
}
