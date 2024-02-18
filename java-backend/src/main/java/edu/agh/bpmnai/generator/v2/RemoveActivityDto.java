package edu.agh.bpmnai.generator.v2;

public record RemoveActivityDto(
        @Description("Background for the specific addition, and how it addresses the problem specified by the user.")
        String background,
        @Description("Activity to remove from the model. Must exist in the model.")
        String activityToRemove
) {
}
