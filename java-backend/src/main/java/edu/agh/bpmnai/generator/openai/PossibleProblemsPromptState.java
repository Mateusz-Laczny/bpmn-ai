package edu.agh.bpmnai.generator.openai;

import edu.agh.bpmnai.generator.openai.model.ChatMessage;

import java.util.List;

public class PossibleProblemsPromptState implements PromptingState {
    private final String userPrompt;
    private final String response;

    public PossibleProblemsPromptState(String userPrompt, String response) {
        this.userPrompt = userPrompt;
        this.response = response;
    }

    @Override
    public List<ChatMessage> getPromptForCurrentState() {
        return List.of(
                ChatMessage.systemMessage("Think about the possible problems that could arise in the described process. " +
                        "When you are done, modify the description you provided accordingly."),
                ChatMessage.userMessage("Users description: \"\"\"" + userPrompt + "\"\"\"\n" +
                        "Your extended description: \n\n\n" + response + "\"\"\"\n")
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
        return new ModelGenerationPromptState();
    }
}
