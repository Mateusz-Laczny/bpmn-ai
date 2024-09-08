package edu.agh.bpmnai.generator.v2.session;

import edu.agh.bpmnai.generator.NewSessionRequest;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.ChatMessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SessionService {

    private static final String SYSTEM_PROMPT = """
                                                You are the world's best business process modelling specialist.
                                                When confronted with a user request, use the provided functions to create a BPMN diagram based on \
                                                the user responses. The functions work in a recursive manner, each function call creates a \
                                                separate subprocess, which is the inserted into the diagram at a specified insertion point. Each \
                                                subprocess has a start and an end node, which can be used to connect subprocesses with each other.\
                                                Remember, that the content between 'BEGIN REQUEST CONTEXT' and 'END REQUEST CONTEXT' is just provided for your information, do not try to modify it or mention it to the user.
                                                Glossary:
                                                - Node: Elements of the BPMN diagram which are connected with sequence flows, such as tasks or gateways.
                                                - Edge: Sequence flow connecting two nodes.
                                                - Insertion point: A Node after which a subprocess will be inserted. If a subprocess start is already connected to that point, it will be reconnected to the end of the inserted subprocess""";

    private final ChatMessageBuilder chatMessageBuilder;

    public SessionService(ChatMessageBuilder chatMessageBuilder) {this.chatMessageBuilder = chatMessageBuilder;}

    public ImmutableSessionState initializeNewSession(NewSessionRequest sessionInfo) {
        var model = new BpmnModel();
        model.addLabelledStartEvent("Start");
        String sessionId = UUID.randomUUID().toString();
        return ImmutableSessionState.builder()
                .sessionId(sessionId)
                .apiKey(sessionInfo.apiKey())
                .model(model)
                .addModelContext(chatMessageBuilder.buildSystemMessage(SYSTEM_PROMPT))
                .putNodeIdToModelInterfaceId(model.getStartEvent(), "start-event")
                .build();
    }

    public ImmutableSessionState addPromptToContext(String promptText, ImmutableSessionState sessionState) {
        return ImmutableSessionState.builder().from(sessionState).addModelContext(chatMessageBuilder.buildUserMessage(
                promptText)).build();
    }
}
