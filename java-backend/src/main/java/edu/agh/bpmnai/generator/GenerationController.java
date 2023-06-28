package edu.agh.bpmnai.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
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
        return bpmnProvider.provideForTextPrompt(prompt);
    }
}
