package edu.agh.bpmnai.generator.bpmn.processtree;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class ProcessTree {
    @Getter
    @Setter
    ProcessTreeNode rootNode;

    Map<String, ProcessTreeNode> idToNodeMap = new HashMap<>();

    public void visitTree(ProcessTreeVisitor visitor) {
        visitNode(rootNode, visitor);
        visitor.afterVisit();
    }

    private void visitNode(ProcessTreeNode node, ProcessTreeVisitor visitor) {
        node.accept(visitor);
        for (ProcessTreeNode childNode : node.getChildren()) {
            visitNode(childNode, visitor);
        }
    }
}
