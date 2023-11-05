package edu.agh.bpmnai.generator.bpmn.processtree;

public class ProcessTreeSequentialNode extends ProcessTreeBaseNode {

    @Override
    public void accept(ProcessTreeVisitor visitor) {
        visitor.visit(this);
    }
}
