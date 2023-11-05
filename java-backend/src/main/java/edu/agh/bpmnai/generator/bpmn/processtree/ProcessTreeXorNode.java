package edu.agh.bpmnai.generator.bpmn.processtree;

public class ProcessTreeXorNode extends ProcessTreeBaseNode {
    @Override
    public <T> T accept(ProcessTreeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
