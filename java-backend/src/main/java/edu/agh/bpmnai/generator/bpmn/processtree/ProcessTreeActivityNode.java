package edu.agh.bpmnai.generator.bpmn.processtree;

import lombok.Getter;

public class ProcessTreeActivityNode extends ProcessTreeBaseNode {

    @Getter
    String activityName;

    public ProcessTreeActivityNode(String activityName) {
        this.activityName = activityName;
    }

    @Override
    public void accept(ProcessTreeVisitor visitor) {
        visitor.visit(this);
    }
}
