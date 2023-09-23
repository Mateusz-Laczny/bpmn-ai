package edu.agh.bpmnai.generator;

import edu.agh.bpmnai.generator.bpmn.BpmnProvider;
import edu.agh.bpmnai.generator.bpmn.model.BpmnFile;
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
    public BpmnFile generateFromTextPrompt(@RequestBody TextPrompt prompt) {
        return bpmnProvider.provideForTextPrompt(prompt);
    }
}
