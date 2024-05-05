package edu.agh.bpmnai.generator.v2.functions.parameter;

public record Activity(
        @Description("Name of the activity.")
        String activityName,
        @Description("Whether this activity marks the end of the process.")
        boolean isProcessEnd
) {
}
