package edu.agh.bpmnai.generator.bpmn.processtree;

import edu.agh.bpmnai.generator.bpmn.model.*;

import java.util.HashSet;
import java.util.Set;

public class ProcessTreeBpmnBuildingVisitor implements ProcessTreeVisitor<String> {

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
    public String visit(ProcessTreeSequentialNode processTreeSequentialNode) {
        for (ProcessTreeNode subtreeInSequence : processTreeSequentialNode.getChildren()) {
            previousElement = subtreeInSequence.accept(this);
        }

        return previousElement;
    }

    @Override
    public String visit(ProcessTreeActivityNode processTreeActivityNode) {
        String activityId = model.addServiceTask(new BpmnServiceTask(processId, processTreeActivityNode.getActivityName()));
        model.addSequenceFlow(new BpmnSequenceFlow(processId, previousElement, activityId, ""));

        if (processTreeActivityNode.hasChildren()) {
            throw new IllegalStateException("Activity node should be a leaf node");
        } else {
            return activityId;
        }
    }

    @Override
    public String visit(ProcessTreeXorNode processTreeXorNode) {
        previousElement = model.addGateway(new BpmnGateway(processId, "", BpmnGatewayType.EXCLUSIVE));
        Set<String> elementsToConnectToClosingGateway = visitNodeChildren(processTreeXorNode);
        String closingGatewayId = model.addGateway(new BpmnGateway(processId, "", BpmnGatewayType.EXCLUSIVE));
        for (String elementId : elementsToConnectToClosingGateway) {
            model.addSequenceFlow(new BpmnSequenceFlow(processId, elementId, closingGatewayId, ""));
        }

        previousElement = closingGatewayId;
        return closingGatewayId;
    }

    @Override
    public void afterVisit() {
        String endEventId = model.addEndEvent(new BpmnEndEvent(processId, ""));
        model.addSequenceFlow(new BpmnSequenceFlow(processId, previousElement, endEventId, ""));
    }

    private Set<String> visitNodeChildren(ProcessTreeNode node) {
        Set<String> endsOfPaths = new HashSet<>();
        for (ProcessTreeNode nodeChild : node.getChildren()) {
            endsOfPaths.add(nodeChild.accept(this));
        }

        return endsOfPaths;
    }
}
