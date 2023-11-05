package edu.agh.bpmnai.generator.bpmn.processtree;

public class ProcessTreeAndNode extends ProcessTreeBaseNode {
    @Override
    public <T> T accept(ProcessTreeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
