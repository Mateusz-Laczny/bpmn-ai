package edu.agh.bpmnai.generator.v2.functions.parameter;

public record Activity(
        @Description("Name of the activity. Must be unique across the diagram.")
        String activityName,
        @Description("Whether this activity marks the end of the process.")
        boolean isProcessEnd
) {
}
