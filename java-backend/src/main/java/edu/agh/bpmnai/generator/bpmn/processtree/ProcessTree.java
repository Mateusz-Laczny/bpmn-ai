package edu.agh.bpmnai.generator.bpmn.processtree;

import edu.agh.bpmnai.generator.Datatypes;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@ToString
@EqualsAndHashCode
public class ProcessTree {
    private final ActivityExecutionMannerNode rootNode;

    public ProcessTree(ActivityExecutionManner activityExecutionManner) {
        rootNode = new ActivityExecutionMannerNode(activityExecutionManner);
    }

    public void replaceActivityWithTree(String activityId, ProcessTree subtree) {
        ActivityNode activityNode = findActivityNode(activityId).orElseThrow();
        ActivityExecutionMannerNode activityParent = activityNode.getParent();
        activityParent.removeChild(activityNode);
        activityParent.addChild(Datatypes.Either.asLeft(subtree.getRootNode()));
    }

    private Optional<ActivityNode> findActivityNode(String activityId) {
        for (ActivityNode activityNode : getActivityNodesInSubtree(Datatypes.Either.asLeft(rootNode))) {
            if (activityNode.getId().equals(activityId)) {
                return Optional.of(activityNode);
            }
        }

        return Optional.empty();
    }

    private List<ActivityNode> getActivityNodesInSubtree(Datatypes.Either<ActivityExecutionMannerNode, ActivityNode> node) {
        if (node.isRight()) {
            return List.of(node.getRight());
        }

        ActivityExecutionMannerNode subtreeRoot = node.getLeft();
        List<ActivityNode> activityNodesInSubtree = new ArrayList<>();
        for (Datatypes.Either<ActivityExecutionMannerNode, ActivityNode> subtreeRootChild : subtreeRoot.getChildren()) {
            activityNodesInSubtree.addAll(getActivityNodesInSubtree(subtreeRootChild));
        }

        return activityNodesInSubtree;
    }
}
