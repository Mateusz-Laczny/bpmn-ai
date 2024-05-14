package edu.agh.bpmnai.generator.v2.functions.parameter;

public record Task(
        @Description("Name of the task.")
        String taskName,
        @Description("Whether this task marks the end of the process.")
        boolean isProcessEnd
) {
}
