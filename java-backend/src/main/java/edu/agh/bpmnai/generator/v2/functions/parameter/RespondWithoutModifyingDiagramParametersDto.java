package edu.agh.bpmnai.generator.v2.functions.parameter;

public record RespondWithoutModifyingDiagramParametersDto(
        @Description("Final message to the user")
        String messageToTheUser
) {
}
