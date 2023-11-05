package edu.agh.bpmnai.generator.bpmn.processtree;

public interface ProcessTreeVisitor<T> {
    T visit(ProcessTreeSequentialNode processTreeSequentialNode);

    T visit(ProcessTreeActivityNode processTreeActivityNode);

    T visit(ProcessTreeXorNode processTreeXorNode);

    void afterVisit();
}
