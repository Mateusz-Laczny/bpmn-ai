package edu.agh.bpmnai.generator.bpmn.model;

public record BpmnFile(String xml) {

    public static BpmnFile fromModel(BpmnModel model) {
        return new BpmnFile(model.asXmlString());
    }
}
