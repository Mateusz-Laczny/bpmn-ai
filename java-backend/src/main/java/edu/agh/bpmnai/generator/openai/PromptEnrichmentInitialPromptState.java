package edu.agh.bpmnai.generator.openai;

import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PromptEnrichmentInitialPromptState implements PromptingState {

    private final String userPrompt;

    @Override
    public List<ChatMessage> getPromptForCurrentState() {
        return List.of(
                ChatMessage.systemMessage("You will be provided a business process description." +
                        " First work out your own business process description based on the one provided by the user." +
                        " Think about all relevant specifics and details. Focus on the happy path in this step."),
                ChatMessage.userMessage(userPrompt)
        );
    }

    @Override
    public boolean hasNextState() {
        return true;
    }

    @Override
    public boolean isFunctionCallingStep() {
        return false;
    }

    @Override
    public PromptingState getNextState(String previousPromptResult) {
        return new PossibleProblemsPromptState(userPrompt, previousPromptResult);
    }
}
