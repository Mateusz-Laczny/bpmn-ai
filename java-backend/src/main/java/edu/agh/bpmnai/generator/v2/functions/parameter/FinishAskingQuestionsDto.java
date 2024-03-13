package edu.agh.bpmnai.generator.v2.functions.parameter;

public record FinishAskingQuestionsDto(
        @Description("Final message to the user")
        String finalMessageToTheUser) {
}
