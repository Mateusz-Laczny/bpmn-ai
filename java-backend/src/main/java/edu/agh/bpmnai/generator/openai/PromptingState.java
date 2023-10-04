package edu.agh.bpmnai.generator.openai;

import edu.agh.bpmnai.generator.openai.model.ChatMessage;

import java.util.List;

public interface PromptingState {
    List<ChatMessage> getPromptForCurrentState();

    boolean hasNextState();

    boolean isFunctionCallingStep();

    PromptingState getNextState(String previousPromptResult);
}
