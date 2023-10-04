package edu.agh.bpmnai.generator.openai;

public class PromptingStrategyFactory {

    public static PromptingState getPromptProvider(PromptingStrategy strategy, String userPrompt) {
        switch (strategy) {
            case PROMPT_ENRICHMENT -> {
                return new PromptEnrichmentInitialPromptState(userPrompt);
            }
            default -> throw new IllegalArgumentException("No switch case defined for value: " + strategy);
        }
    }
}
