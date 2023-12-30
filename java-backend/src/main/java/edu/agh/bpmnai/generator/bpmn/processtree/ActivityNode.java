package edu.agh.bpmnai.generator.bpmn.processtree;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class ActivityNode {
    private final String id;
    private final String activityName;
    private final ActivityExecutionMannerNode parent;

    public ActivityNode(String id, String activityName, ActivityExecutionMannerNode parent) {
        this.id = id;
        this.activityName = activityName;
        this.parent = parent;
    }
}
