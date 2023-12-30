package edu.agh.bpmnai.generator.bpmn.processtree;

import edu.agh.bpmnai.generator.Datatypes;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@EqualsAndHashCode
public class ActivityExecutionMannerNode {

    @Getter
    private final ActivityExecutionManner nodeType;

    @Getter
    private final List<Datatypes.Either<ActivityExecutionMannerNode, ActivityNode>> children;

    public ActivityExecutionMannerNode(ActivityExecutionManner nodeType) {
        this.nodeType = nodeType;
        this.children = new ArrayList<>();
    }

    public void addChild(Datatypes.Either<ActivityExecutionMannerNode, ActivityNode> newChild) {
        this.children.add(newChild);
    }

    public void addActivityNode(String activityId, String activityName) {
        ActivityNode activityNode = new ActivityNode(activityId, activityName, this);
        children.add(Datatypes.Either.asRight(activityNode));
    }

    public void removeChild(ActivityNode activityNode) {
        children.remove(Datatypes.Either.asRight(activityNode));
    }
}
