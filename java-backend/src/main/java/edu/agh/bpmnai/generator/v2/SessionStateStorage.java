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
        sessionState = new SessionState(List.of("You are the world's best business process modelling specialist. You'll receive a 500$ tip, if you follow ALL of the rules:\n" +
                                                "- make as detailed model as possible\n" +
                                                "- reason about every user request\n" +
                                                "- do not mention functions in user-facing messages\n" +
                                                "- use as few function calls as possible\n" +
                                                "- use the provided functions to create the model, do not try to include the model as a text message"
        ));
        return sessionState;
    }

    public SessionState getCurrentState() {
        return sessionState;
    }

    public void clearState() {
        sessionState = initializeEmptySessionState();
    }
}
