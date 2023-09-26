package edu.agh.bpmnai.generator.openai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenAIChatSessionFactory {

    private final OpenAIChatCompletionApi chatCompletionApi;

    @Autowired
    public OpenAIChatSessionFactory(OpenAIChatCompletionApi chatCompletionApi) {
        this.chatCompletionApi = chatCompletionApi;
    }

    public OpenAIChatSession createNewSession(OpenAI.OpenAIModel model, float initialTemperature) {
        return OpenAIChatSession.newSession(chatCompletionApi, model, initialTemperature);
    }
}
