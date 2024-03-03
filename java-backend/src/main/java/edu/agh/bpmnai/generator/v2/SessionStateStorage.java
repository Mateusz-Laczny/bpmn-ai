package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.v2.session.SessionState;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionStateStorage {

    private SessionState sessionState;

    public SessionStateStorage() {
        sessionState = initializeEmptySessionState();
    }

    private SessionState initializeEmptySessionState() {
        final SessionState sessionState;
        sessionState = new SessionState(List.of("You are the world's best business process modelling specialist. When confronted with a user request, ask questions to gather as much necessary information as possible, the use provided functions to create a BPMN diagram based on the user responses"));
        return sessionState;
    }

    public SessionState getCurrentState() {
        return sessionState;
    }

    public void clearState() {
        sessionState = initializeEmptySessionState();
    }
}
