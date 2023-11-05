package edu.agh.bpmnai.generator.bpmn.processtree;

import lombok.Getter;

public class ProcessTreeActivityNode extends ProcessTreeBaseNode {

    @Getter
    String activityName;

    public ProcessTreeActivityNode(String activityName) {
        this.activityName = activityName;
    }

    @Override
    public <T> T accept(ProcessTreeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
