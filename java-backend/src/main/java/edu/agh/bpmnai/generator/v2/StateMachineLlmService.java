package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.bpmn.layouting.TopologicalSortBpmnLayouting;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.ChangelogSnapshot;
import edu.agh.bpmnai.generator.v2.session.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.PROMPTING_FINISHED;

@Service
@Slf4j
public class StateMachineLlmService implements LlmService {

    private final SessionStateStore sessionStateStore;
    private final ReasonAboutTasksAndProcessFlowState reasonAboutTasksAndProcessFlowState;
    private final ModifyModelState modifyModelState;
    private final DecideWhetherToModifyTheModelState decideWhetherToModifyTheModelState;
    private final ModelPostProcessing modelPostProcessing;
    private final TopologicalSortBpmnLayouting bpmnLayouting;

    @Autowired
    public StateMachineLlmService(
            SessionStateStore sessionStateStore,
            ReasonAboutTasksAndProcessFlowState reasonAboutTasksAndProcessFlowState,
            ModifyModelState modifyModelState,
            DecideWhetherToModifyTheModelState decideWhetherToModifyTheModelState,
            ModelPostProcessing modelPostProcessing,
            TopologicalSortBpmnLayouting bpmnLayouting
    ) {
        this.sessionStateStore = sessionStateStore;
        this.reasonAboutTasksAndProcessFlowState = reasonAboutTasksAndProcessFlowState;
        this.modifyModelState = modifyModelState;
        this.decideWhetherToModifyTheModelState = decideWhetherToModifyTheModelState;
        this.modelPostProcessing = modelPostProcessing;
        this.bpmnLayouting = bpmnLayouting;
    }

    @Override
    public UserRequestResponse getResponse(String sessionId) {
        ImmutableSessionState sessionState = sessionStateStore.getSessionState(sessionId).orElseThrow();
        String userPrompt = sessionState.lastAddedMessage().content();
        while (sessionState.sessionStatus() != PROMPTING_FINISHED) {
            sessionState = switch (sessionState.sessionStatus()) {
                case DECIDE_WHETHER_TO_MODIFY_THE_MODEL -> decideWhetherToModifyTheModelState.process(
                        userPrompt,
                        sessionState
                );
                case NEW, REASON_ABOUT_TASKS_AND_PROCESS_FLOW -> reasonAboutTasksAndProcessFlowState.process(
                        userPrompt,
                        sessionState
                );
                case MODIFY_MODEL -> modifyModelState.process(sessionState);
                default ->
                        throw new IllegalStateException("Unexpected session status '%s'".formatted(sessionState.sessionStatus()));
            };
            log.info("New session status: '{}'", sessionState.sessionStatus());
        }

        sessionState = modelPostProcessing.apply(sessionState);
        sessionStateStore.saveSessionState(sessionState);
        BpmnModel finalModel = sessionState.bpmnModel();
        ChangelogSnapshot changelogSnapshot = finalModel.getChangeLogSnapshot();
        BpmnModel layoutedModel = bpmnLayouting.layoutModel(sessionState.bpmnModel());
        return new UserRequestResponse(
                sessionState.lastUserFacingMessage().orElse(null),
                layoutedModel.asXmlString(),
                changelogSnapshot.nodeModificationLogs(),
                changelogSnapshot.flowModificationLogs()
        );
    }

}
