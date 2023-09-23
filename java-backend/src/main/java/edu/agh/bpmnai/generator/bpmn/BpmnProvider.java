package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.TextPrompt;
import edu.agh.bpmnai.generator.bpmn.model.BpmnFile;

public interface BpmnProvider {
    BpmnFile provideForTextPrompt(TextPrompt prompt);
}
