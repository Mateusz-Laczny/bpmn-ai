package edu.agh.bpmnai.generator.v2.session;

import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.OpenAIChatCompletionApi;
import edu.agh.bpmnai.generator.v2.ChatMessageBuilder;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.MODIFY_MODEL;

@Service
@Slf4j
public class ReasonAboutTasksAndProcessFlowState {

    private final OpenAIChatCompletionApi chatCompletionApi;

    private final OpenAI.OpenAIModel usedModel;

    private final SessionStateStore sessionStateStore;

    private final ChatMessageBuilder chatMessageBuilder;

    @Autowired
    public ReasonAboutTasksAndProcessFlowState(OpenAIChatCompletionApi chatCompletionApi, OpenAI.OpenAIModel usedModel, SessionStateStore sessionStateStore, ChatMessageBuilder chatMessageBuilder) {
        this.chatCompletionApi = chatCompletionApi;
        this.usedModel = usedModel;
        this.sessionStateStore = sessionStateStore;
        this.chatMessageBuilder = chatMessageBuilder;
    }

    public SessionStatus process(String userRequestContent) {
        sessionStateStore.appendMessage(chatMessageBuilder.buildUserMessage(userRequestContent));
        var promptMessage = chatMessageBuilder.buildSystemMessage("Now, reason about and describe the plan of implementing the user request. Use BPMN terminology. Remember to think about possible edge cases and paths different than the happy path. Don't try to create diagram, this will be done in the next step.");
        sessionStateStore.appendMessage(promptMessage);
        ChatMessageDto chatResponse = chatCompletionApi.sendRequest(usedModel, sessionStateStore.messages(), null, null);
        log.info("Response: '{}'", chatResponse.content());
        sessionStateStore.appendMessage(chatResponse);
        return MODIFY_MODEL;
    }
}
