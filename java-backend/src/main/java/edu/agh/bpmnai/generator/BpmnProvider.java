package edu.agh.bpmnai.generator;

import edu.agh.bpmnai.generator.bpmn.model.BpmnFile;

public interface BpmnProvider {
    BpmnFile provideForTextPrompt(TextPrompt prompt);
}
