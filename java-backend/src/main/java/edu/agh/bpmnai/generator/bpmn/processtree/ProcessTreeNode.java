package edu.agh.bpmnai.generator.bpmn.processtree;

import java.util.List;

public interface ProcessTreeNode {
    void accept(ProcessTreeVisitor visitor);

    void setParent(ProcessTreeNode parent);

    List<ProcessTreeNode> getChildren();

    void addChild(ProcessTreeNode newChild);
}
