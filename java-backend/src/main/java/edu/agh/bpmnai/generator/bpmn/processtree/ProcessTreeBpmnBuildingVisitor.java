package edu.agh.bpmnai.generator.bpmn.processtree;

import edu.agh.bpmnai.generator.bpmn.model.*;

public class ProcessTreeBpmnBuildingVisitor implements ProcessTreeVisitor {

    private final String processId;
    BpmnModel model = new BpmnModel();
    private String previousElement;

    public ProcessTreeBpmnBuildingVisitor() {
        processId = model.addProcess(new BpmnProcess(""));
        previousElement = model.addStartEvent(new BpmnStartEvent(processId, ""));
    }

    public BpmnModel getModel() {
        return model.getCopy();
    }

    @Override
    public void visit(ProcessTreeSequentialNode processTreeSequentialNode) {
        visitNodeChildren(processTreeSequentialNode);
    }

    @Override
    public void visit(ProcessTreeActivityNode processTreeActivityNode) {
        String nextPreviousElement = model.addServiceTask(new BpmnServiceTask(processId, processTreeActivityNode.getActivityName()));
        model.addSequenceFlow(new BpmnSequenceFlow(processId, previousElement, nextPreviousElement, ""));
        previousElement = nextPreviousElement;
        visitNodeChildren(processTreeActivityNode);
    }

    @Override
    public void afterVisit() {
        String endEventId = model.addEndEvent(new BpmnEndEvent(processId, ""));
        model.addSequenceFlow(new BpmnSequenceFlow(processId, previousElement, endEventId, ""));
    }

    private void visitNodeChildren(ProcessTreeNode node) {
        for (ProcessTreeNode nodeChild : node.getChildren()) {
            nodeChild.accept(this);
        }
    }
}
