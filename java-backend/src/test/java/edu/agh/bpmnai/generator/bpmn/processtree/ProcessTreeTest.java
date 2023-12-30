package edu.agh.bpmnai.generator.bpmn.processtree;

import edu.agh.bpmnai.generator.Datatypes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessTreeTest {
    @Test
    void shouldCorrectlyAddActivityNode() {
        var tree = new ProcessTree(ActivityExecutionManner.SEQUENCE);

        tree.getRootNode().addActivityNode("1", "Activity");

        assertEquals(List.of(Datatypes.Either.asRight(new ActivityNode("1", "Activity", tree.getRootNode()))), tree.getRootNode().getChildren());
    }

    @Test
    void shouldCorrectlyReplaceActivityNodeWithSubtree() {
        var tree = new ProcessTree(ActivityExecutionManner.SEQUENCE);
        tree.getRootNode().addActivityNode("1", "Activity");
        var newSubtree = new ProcessTree(ActivityExecutionManner.AND);
        newSubtree.getRootNode().addActivityNode("2", "Activity2");

        tree.replaceActivityWithTree("1", newSubtree);

        assertEquals(1, tree.getRootNode().getChildren().size());
        assertEquals(new ActivityNode("2", "Activity2", tree.getRootNode().getChildren().get(0).getLeft()), tree.getRootNode().getChildren().get(0).getLeft().getChildren().get(0).getRight());
    }
}