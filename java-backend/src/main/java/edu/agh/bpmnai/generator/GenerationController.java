package edu.agh.bpmnai.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/generate")
public class GenerationController {

    private final BpmnProvider bpmnProvider;

    @Autowired
    public GenerationController(BpmnProvider bpmnProvider) {
        this.bpmnProvider = bpmnProvider;
    }

    @PostMapping("/from/text")
    public BpmnFile generateFromTextPrompt(@RequestBody TextPrompt prompt) throws JsonProcessingException {
        System.out.println(prompt);
        return bpmnProvider.provideForTextPrompt(prompt);
    }
}
