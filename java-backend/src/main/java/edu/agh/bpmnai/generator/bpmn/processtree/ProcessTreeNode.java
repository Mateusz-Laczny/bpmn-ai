package edu.agh.bpmnai.generator.bpmn.processtree;

import java.util.List;

public interface ProcessTreeNode {
    <T> T accept(ProcessTreeVisitor<T> visitor);

    void setParent(ProcessTreeNode parent);

    List<ProcessTreeNode> getChildren();

    void addChild(ProcessTreeNode newChild);

    boolean hasChildren();
}
