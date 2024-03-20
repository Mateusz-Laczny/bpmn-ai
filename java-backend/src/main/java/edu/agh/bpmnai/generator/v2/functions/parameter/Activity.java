package edu.agh.bpmnai.generator.v2.functions.parameter;

public record Activity(
        @Description("Name of the activity")
        String activityName,
        @Description("How to handle duplicate activities - whether to add a new instance or use an existing one")
        DuplicateHandlingStrategy howToHandleDuplicates) {
}
