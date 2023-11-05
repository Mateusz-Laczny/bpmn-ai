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

    public void visitRoot(ProcessTreeVisitor visitor) {
        rootNode.accept(visitor);
    }
}
