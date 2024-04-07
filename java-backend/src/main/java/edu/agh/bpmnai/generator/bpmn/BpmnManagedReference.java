package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;

public class BpmnManagedReference {
    private BpmnModel model;

    public BpmnManagedReference(BpmnModel model) {
        this.model = model;
    }

    public BpmnModel getCurrentValue() {
        return model.getCopy();
    }

    public void setValue(BpmnModel model) {
        this.model = model.getCopy();
    }
}
