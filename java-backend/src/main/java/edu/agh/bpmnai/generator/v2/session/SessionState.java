package edu.agh.bpmnai.generator.v2.session;

import com.google.common.collect.BiMap;
import com.google.common.primitives.UnsignedInteger;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

import static edu.agh.bpmnai.generator.v2.session.SessionStatus.NEW;

@Value.Immutable
public abstract class SessionState {

    public abstract String sessionId();

    @Value.Default
    public SessionStatus sessionStatus() {
        return NEW;
    }

    public abstract List<ChatMessageDto> modelContext();

    public abstract List<String> userFacingMessages();

    public abstract BiMap<String, String> nodeIdToModelInterfaceId();

    abstract BpmnModel model();

    public BpmnModel bpmnModel() {
        return model().getCopy();
    }

    @Value.Default
    public UnsignedInteger numberOfFailedFunctionCalls() {
        return UnsignedInteger.ZERO;
    }

    public ChatMessageDto lastAddedMessage() {
        return modelContext().get(modelContext().size() - 1);
    }

    public Optional<String> lastUserFacingMessage() {
        List<String> userFacingMessages = userFacingMessages();
        if (userFacingMessages.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(userFacingMessages.get(userFacingMessages.size() - 1));
    }

    public int numberOfMessages() {
        return modelContext().size();
    }

    public Optional<String> getModelInterfaceId(String nodeId) {
        return Optional.ofNullable(nodeIdToModelInterfaceId().get(nodeId));
    }

    public Optional<String> getNodeId(String modelInterfaceId) {
        return Optional.ofNullable(nodeIdToModelInterfaceId().inverse().get(modelInterfaceId));
    }
}
