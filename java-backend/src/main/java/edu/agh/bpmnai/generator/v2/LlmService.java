package edu.agh.bpmnai.generator.v2;

public interface LlmService {
    UserRequestResponse getResponse(String userMessageContent);

    void startNewConversation();
}
