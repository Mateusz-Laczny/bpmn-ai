package edu.agh.bpmnai.generator.bpmn.processtree;

public interface ProcessTreeVisitor {
    void visit(ProcessTreeSequentialNode processTreeSequentialNode);

    void visit(ProcessTreeActivityNode processTreeActivityNode);

    void afterVisit();
}
