package edu.agh.bpmnai.generator;

import edu.agh.bpmnai.generator.bpmn.BpmnProvider;
import edu.agh.bpmnai.generator.bpmn.model.BpmnFile;
import edu.agh.bpmnai.generator.v2.LlmService;
import edu.agh.bpmnai.generator.v2.UserRequestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/generate")
@Slf4j
public class GenerationController {

    private final BpmnProvider bpmnProvider;

    private final LlmService llmService;

    @Autowired
    public GenerationController(BpmnProvider bpmnProvider, LlmService llmService) {
        this.bpmnProvider = bpmnProvider;
        this.llmService = llmService;
    }

    @PostMapping("/from/text")
    public BpmnFile generateFromTextPrompt(@RequestBody TextPrompt prompt) {
        return bpmnProvider.provideForTextPrompt(prompt);
    }

    @PostMapping("v2/send/message")
    public UserRequestResponse sendMessage(@RequestBody TextPrompt newMessage) {
        log.info("Received request on endpoint 'v2/send/message': requestBody: {}", newMessage);
        return llmService.getResponse(newMessage.content());
    }

    @PostMapping("v2/start")
    public void startNewConversation() {
        log.info("New conversation started");
        llmService.startNewConversation();
    }
}
