package edu.agh.bpmnai.generator.v2.functions.parameter;

public record RetrospectiveSummary(
        @Description("Description of the current state of the process diagram. Includes information about all executed function calls up to this point, and the current state, especially for the possible failure cases. Must be at least 2 sentences long")
        String summaryText
) {
}
