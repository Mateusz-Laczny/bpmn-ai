package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
import edu.agh.bpmnai.generator.bpmn.layouting.TopologicalSortBpmnLayouting;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.ChangelogSnapshot;
import edu.agh.bpmnai.generator.v2.session.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.END;
import static edu.agh.bpmnai.generator.v2.session.SessionStatus.REASON_ABOUT_TASKS_AND_PROCESS_FLOW;

@Service
@Slf4j
public class LlmService {

    private final SessionStateStore sessionStateStore;

    private final ConversationHistoryStore conversationHistoryStore;

    private final AskQuestionsState askQuestionsState;
    private final ReasonAboutTasksAndProcessFlowState reasonAboutTasksAndProcessFlowState;
    private final ModifyModelState modifyModelState;

    private final FixErrorsInModelState fixErrorsInModelState;

    private final ChatMessageBuilder chatMessageBuilder;

    private final ModelPostProcessing modelPostProcessing;

    private final TopologicalSortBpmnLayouting bpmnLayouting;

    @Autowired
    public LlmService(
            SessionStateStore sessionStateStore,
            ConversationHistoryStore conversationHistoryStore,
            AskQuestionsState askQuestionsState,
            ReasonAboutTasksAndProcessFlowState reasonAboutTasksAndProcessFlowState,
            ModifyModelState modifyModelState,
            FixErrorsInModelState fixErrorsInModelState,
            ChatMessageBuilder chatMessageBuilder,
            ModelPostProcessing modelPostProcessing, TopologicalSortBpmnLayouting bpmnLayouting
    ) {
        this.sessionStateStore = sessionStateStore;
        this.conversationHistoryStore = conversationHistoryStore;
        this.askQuestionsState = askQuestionsState;
        this.reasonAboutTasksAndProcessFlowState = reasonAboutTasksAndProcessFlowState;
        this.modifyModelState = modifyModelState;
        this.fixErrorsInModelState = fixErrorsInModelState;
        this.chatMessageBuilder = chatMessageBuilder;
        this.modelPostProcessing = modelPostProcessing;
        this.bpmnLayouting = bpmnLayouting;
    }

    public UserRequestResponse getResponse(String userMessageContent) {
        SessionStatus sessionState = REASON_ABOUT_TASKS_AND_PROCESS_FLOW;
        while (sessionState != END) {
            sessionState = switch (sessionState) {
                case ASK_QUESTIONS -> askQuestionsState.process(userMessageContent);
                case REASON_ABOUT_TASKS_AND_PROCESS_FLOW -> reasonAboutTasksAndProcessFlowState.process(
                        userMessageContent);
                case MODIFY_MODEL -> modifyModelState.process(userMessageContent);
                case FIX_ERRORS -> fixErrorsInModelState.process(userMessageContent);
                default -> throw new IllegalStateException("Unexpected session state '%s'".formatted(sessionState));
            };
            log.info("New state: '{}'", sessionState);
        }

        var modelReference = new BpmnManagedReference(sessionStateStore.model());
        modelPostProcessing.apply(modelReference);
        BpmnModel finalModel = modelReference.getCurrentValue();
        ChangelogSnapshot changelogSnapshot = finalModel.getChangeLogSnapshot();

        BpmnModel layoutedModel = bpmnLayouting.layoutModel(finalModel);
        return new UserRequestResponse(
                conversationHistoryStore.getLastMessage().orElse(""),
                layoutedModel.asXmlString(),
                changelogSnapshot.nodeModificationLogs(),
                changelogSnapshot.flowModificationLogs()
        );
    }

    public void startNewConversation() {
        sessionStateStore.clearState();
        conversationHistoryStore.clearMessages();
        sessionStateStore.appendMessage(chatMessageBuilder.buildSystemMessage(
                "You are the world's best business process modelling specialist. "
                + "When confronted with a user request, ask questions to gather as much necessary information as "
                + "possible, " + "the use provided functions to create a BPMN diagram based on the user responses."
                + "Remember, that the content between 'BEGIN REQUEST CONTEXT' and 'END REQUEST CONTEXT' is just "
                + "provided"
                + "for your information, do not try to modify it or mention it to the user."));
    }
}
