package edu.agh.bpmnai.generator;

import edu.agh.bpmnai.generator.bpmn.BpmnProvider;
import edu.agh.bpmnai.generator.bpmn.layouting.BpmnSemanticLayouting;
import edu.agh.bpmnai.generator.bpmn.model.BpmnFile;
import edu.agh.bpmnai.generator.v2.ConversationMessage;
import edu.agh.bpmnai.generator.v2.ConversationService;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import edu.agh.bpmnai.generator.v2.session.SessionStateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static java.util.stream.Collectors.toList;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/generate")
@Slf4j
public class GenerationController {

    private final BpmnProvider bpmnProvider;

    private final ConversationService conversationService;

    private final BpmnSemanticLayouting bpmnSemanticLayouting;

    @Autowired
    public GenerationController(BpmnProvider bpmnProvider, ConversationService conversationService, BpmnSemanticLayouting bpmnSemanticLayouting) {
        this.bpmnProvider = bpmnProvider;
        this.conversationService = conversationService;
        this.bpmnSemanticLayouting = bpmnSemanticLayouting;
    }

    @PostMapping("/from/text")
    public BpmnFile generateFromTextPrompt(@RequestBody TextPrompt prompt) {
        return bpmnProvider.provideForTextPrompt(prompt);
    }

    @PostMapping("v2/send/message")
    public SessionStateDto sendMessage(@RequestBody TextPrompt newMessage) {
        log.info("Received request on endpoint 'v2/send/message': requestBody: {}", newMessage);
        SessionState updatedSessionState = conversationService.newMessageReceived(newMessage);
        return new SessionStateDto(
                updatedSessionState.messages().stream()
                        .filter(message -> (!message.role().equals("tool") && message.toolCalls() == null))
                        .map(message -> new ConversationMessage(message.role(), message.content()))
                        .collect(toList()),
                bpmnSemanticLayouting.layoutModel(updatedSessionState.model()).asXmlString()
        );
    }
}
