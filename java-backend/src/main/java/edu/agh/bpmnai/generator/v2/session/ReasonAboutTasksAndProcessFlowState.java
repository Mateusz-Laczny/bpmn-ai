package edu.agh.bpmnai.generator.v2.session;

import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.OpenAIChatCompletionApi;
import edu.agh.bpmnai.generator.v2.ChatMessageBuilder;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static edu.agh.bpmnai.generator.v2.session.ModifyModelState.FUNCTIONS_FOR_MODIFYING_THE_MODEL;
import static edu.agh.bpmnai.generator.v2.session.SessionStatus.MODIFY_MODEL;

@Service
@Slf4j
public class ReasonAboutTasksAndProcessFlowState {

    private static final String PROMPT_TEMPLATE =
            """
            Now, reason about and describe how to fulfil the user request using the provided functions. Use at \
            least 2000 \
            characters. Remember to include possible edge cases and paths different than the happy \
            path. Don't try to create diagram, this will be done in the next step.\
            After you finish the description, think of possible critiques and change your description to \
            address them by modifying your plan.
            BEGIN USER REQUEST
            %s
            END USER REQUEST""";
    private final OpenAIChatCompletionApi chatCompletionApi;
    private final OpenAI.OpenAIModel usedModel;
    private final ChatMessageBuilder chatMessageBuilder;

    @Autowired
    public ReasonAboutTasksAndProcessFlowState(
            OpenAIChatCompletionApi chatCompletionApi,
            OpenAI.OpenAIModel usedModel,
            ChatMessageBuilder chatMessageBuilder
    ) {
        this.chatCompletionApi = chatCompletionApi;
        this.usedModel = usedModel;
        this.chatMessageBuilder = chatMessageBuilder;
    }

    public ImmutableSessionState process(String userRequestText, ImmutableSessionState sessionState) {
        List<ChatMessageDto> updatedModelContext = new ArrayList<>(sessionState.modelContext());
        ChatMessageDto promptDto = chatMessageBuilder.buildUserMessage(PROMPT_TEMPLATE.formatted(userRequestText));
        updatedModelContext.add(promptDto);
        ChatMessageDto chatCompletion = chatCompletionApi.sendRequest(
                usedModel,
                updatedModelContext,
                FUNCTIONS_FOR_MODIFYING_THE_MODEL,
                "none"
        );

        log.info("Response: '{}'", chatCompletion.content());

        updatedModelContext.add(chatCompletion);
        return ImmutableSessionState.builder().from(sessionState)
                .sessionStatus(MODIFY_MODEL)
                .modelContext(updatedModelContext)
                .build();
    }
}
