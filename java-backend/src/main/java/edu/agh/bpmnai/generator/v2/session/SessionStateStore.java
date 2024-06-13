package edu.agh.bpmnai.generator.v2.session;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SessionStateStore {

    private final ConcurrentMap<String, ImmutableSessionState> sessionStates = new ConcurrentHashMap<>();

    public Optional<ImmutableSessionState> getSessionState(String sessionId) {
        return Optional.ofNullable(sessionStates.get(sessionId));
    }

    public void saveSessionState(ImmutableSessionState sessionState) {
        sessionStates.put(sessionState.sessionId(), sessionState);
    }
}
