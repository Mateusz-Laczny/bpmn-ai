package edu.agh.bpmnai.generator.bpmn.processtree;

import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class ProcessTreeBaseNode implements ProcessTreeNode {

    private final List<ProcessTreeNode> children = new ArrayList<>();

    @Setter
    private ProcessTreeNode parent = null;

    @Override
    public List<ProcessTreeNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void addChild(ProcessTreeNode newChild) {
        children.add(newChild);
        newChild.setParent(this);
    }

    public void addChildren(Collection<ProcessTreeNode> newChildren) {
        for (ProcessTreeNode newChild : newChildren) {
            addChild(newChild);
        }
    }
}
