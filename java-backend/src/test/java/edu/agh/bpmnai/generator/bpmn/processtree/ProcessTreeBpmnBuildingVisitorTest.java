package edu.agh.bpmnai.generator.bpmn.processtree;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import org.junit.jupiter.api.Test;

class ProcessTreeBpmnBuildingVisitorTest {

    @Test
    void shouldBuildSimpleSequentialModel() {
        ProcessTree processTree = new ProcessTree();
        ProcessTreeSequentialNode treeRoot = new ProcessTreeSequentialNode();
        processTree.setRootNode(treeRoot);
        treeRoot.addChild(new ProcessTreeActivityNode("Activity A"));
        treeRoot.addChild(new ProcessTreeActivityNode("Activity B"));
        treeRoot.addChild(new ProcessTreeActivityNode("Activity C"));
        var visitor = new ProcessTreeBpmnBuildingVisitor();

        processTree.visitTree(visitor);
        BpmnModel model = visitor.getModel();

        System.out.println(model.asXmlString());
    }

    @Test
    void shouldBuildNestedSequentialModel() {
        ProcessTree processTree = new ProcessTree();
        ProcessTreeSequentialNode treeRoot = new ProcessTreeSequentialNode();
        processTree.setRootNode(treeRoot);
        treeRoot.addChild(new ProcessTreeActivityNode("Activity A"));

        var nestedSequentialSubtreeRoot = new ProcessTreeSequentialNode();
        nestedSequentialSubtreeRoot.addChild(new ProcessTreeActivityNode("Activity B"));
        nestedSequentialSubtreeRoot.addChild(new ProcessTreeActivityNode("Activity C"));

        treeRoot.addChild(nestedSequentialSubtreeRoot);

        var visitor = new ProcessTreeBpmnBuildingVisitor();

        processTree.visitTree(visitor);
        BpmnModel model = visitor.getModel();

        System.out.println(model.asXmlString());
    }
}