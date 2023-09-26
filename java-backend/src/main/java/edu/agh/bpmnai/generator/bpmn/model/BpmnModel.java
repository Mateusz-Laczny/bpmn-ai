package edu.agh.bpmnai.generator.bpmn.model;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.Objects;

public final class BpmnModel {
    private final BpmnModelInstance modelInstance;

    private BpmnModel(BpmnModelInstance modelInstanceToCopy) {
        this.modelInstance = modelInstanceToCopy.clone();
    }

    public BpmnModel() {
        modelInstance = Bpmn.createEmptyModel();
        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setTargetNamespace("http://camunda.org/examples");
        modelInstance.setDefinitions(definitions);
        Bpmn.validateModel(modelInstance);
    }

    private static <T extends BpmnModelElementInstance> T createElementWithParent(BpmnModelElementInstance parentElement, String id, Class<T> elementClass) {
        T element = parentElement.getModelInstance().newInstance(elementClass);
        element.setAttributeValue("id", id, true);
        parentElement.addChildElement(element);
        return element;
    }

    private static SequenceFlow createSequenceFlow(Process process, String id, FlowNode from, FlowNode to) {
        SequenceFlow sequenceFlow = createElementWithParent(process, id, SequenceFlow.class);
        process.addChildElement(sequenceFlow);
        sequenceFlow.setSource(from);
        from.getOutgoing().add(sequenceFlow);
        sequenceFlow.setTarget(to);
        to.getIncoming().add(sequenceFlow);
        return sequenceFlow;
    }

    public String asXmlString() {
        Bpmn.validateModel(modelInstance);
        return Bpmn.convertToString(modelInstance);
    }

    public void addUserTask(BpmnUserTask userTask) {
        if (doesIdExist(userTask.id())) {
            throw new IllegalArgumentException("Id \"" + userTask.id() + "\" is not unique");
        }

        Process process = modelInstance.getModelElementById(userTask.processId());
        UserTask userTaskElement = createElementWithParent(process, userTask.id(), UserTask.class);
        userTaskElement.setAttributeValue("name", userTask.name());
        userTaskElement.setCamundaAssignee(userTask.assignee());
    }

    public void addServiceTask(BpmnServiceTask serviceTask) {
        if (doesIdExist(serviceTask.id())) {
            throw new IllegalArgumentException("Id \"" + serviceTask.id() + "\" is not unique");
        }

        Process process = modelInstance.getModelElementById(serviceTask.processId());
        ServiceTask serviceTaskElement = createElementWithParent(process, serviceTask.id(), ServiceTask.class);
        serviceTaskElement.setAttributeValue("name", serviceTask.name());
    }

    public void addProcess(BpmnProcess process) {
        if (doesIdExist(process.id())) {
            throw new IllegalArgumentException("Id \"" + process.id() + "\" is not unique");
        }

        Process processElement = createElementWithParent(modelInstance.getDefinitions(), process.id(), Process.class);
        processElement.setAttributeValue("name", process.name());
    }

    public void addGateway(BpmnGateway gateway) {
        if (doesIdExist(gateway.id())) {
            throw new IllegalArgumentException("Id \"" + gateway.id() + "\" is not unique");
        }

        Process process = modelInstance.getModelElementById(gateway.processId());
        Gateway gatewayElement;
        switch (gateway.type()) {
            case EXCLUSIVE -> gatewayElement = createElementWithParent(process, gateway.id(), ExclusiveGateway.class);
            case INCLUSIVE -> gatewayElement = createElementWithParent(process, gateway.id(), InclusiveGateway.class);
            default -> throw new IllegalStateException("Unexpected gateway type value: " + gateway.type());
        }

        gatewayElement.setAttributeValue("name", gateway.name());
    }

    public void addStartEvent(BpmnStartEvent startEvent) {
        if (doesIdExist(startEvent.id())) {
            throw new IllegalArgumentException("Id \"" + startEvent.id() + "\" is not unique");
        }

        Process process = modelInstance.getModelElementById(startEvent.processId());
        StartEvent startEventElement = createElementWithParent(process, startEvent.id(), StartEvent.class);
        startEventElement.setAttributeValue("name", startEvent.name());
    }

    public void addEndEvent(BpmnEndEvent endEvent) {
        if (doesIdExist(endEvent.id())) {
            throw new IllegalArgumentException("Id \"" + endEvent.id() + "\" is not unique");
        }

        Process process = modelInstance.getModelElementById(endEvent.processId());
        EndEvent endEventElement = createElementWithParent(process, endEvent.id(), EndEvent.class);
        endEventElement.setAttributeValue("name", endEvent.name());
    }

    public void addIntermediateEvent(BpmnIntermediateEvent intermediateEvent) {
        if (doesIdExist(intermediateEvent.id())) {
            throw new IllegalArgumentException("Id \"" + intermediateEvent.id() + "\" is not unique");
        }

        Process process = modelInstance.getModelElementById(intermediateEvent.processId());
        if (intermediateEvent.catchEvent()) {
            CatchEvent catchEventElement = createElementWithParent(process, intermediateEvent.id(), IntermediateCatchEvent.class);
            catchEventElement.setAttributeValue("name", intermediateEvent.name());
        } else {
            ThrowEvent throwEventElement = createElementWithParent(process, intermediateEvent.id(), IntermediateThrowEvent.class);
            throwEventElement.setAttributeValue("name", intermediateEvent.name());
        }
    }

    public void addMessageEvent(BpmnMessageEvent messageEvent) {
        if (doesIdExist(messageEvent.eventId())) {
            throw new IllegalArgumentException("Id \"" + messageEvent.eventId() + "\" is not unique");
        } else if (doesIdExist(messageEvent.messageId())) {
            throw new IllegalArgumentException("Id \"" + messageEvent.messageId() + "\" is not unique");
        }

        Message message = createElementWithParent(modelInstance.getDefinitions(), messageEvent.messageId(), Message.class);
        message.setName(messageEvent.messageName());
        MessageEventDefinition messageEventDefinition = createElementWithParent(modelInstance.getModelElementById(messageEvent.parentElementId()), messageEvent.eventId(), MessageEventDefinition.class);
        messageEventDefinition.setMessage(message);
    }

    public void addSignalEvent(BpmnSignalEvent signalEvent) {
        if (doesIdExist(signalEvent.signalEventId())) {
            throw new IllegalArgumentException("Id \"" + signalEvent.signalId() + "\" is not unique");
        }

        if (doesIdExist(signalEvent.signalEventId())) {
            throw new IllegalArgumentException("Id \"" + signalEvent.signalEventId() + "\" is not unique");
        }

        Signal signal = createElementWithParent(modelInstance.getDefinitions(), signalEvent.signalId(), Signal.class);
        signal.setName(signalEvent.signalName());
        SignalEventDefinition signalEventDefinition = createElementWithParent(modelInstance.getModelElementById(signalEvent.parentElementId()), signalEvent.signalEventId(), SignalEventDefinition.class);
        signalEventDefinition.setSignal(signal);
    }

    public void addSequenceFlow(BpmnSequenceFlow sequenceFlow) {
        if (doesIdExist(sequenceFlow.id())) {
            throw new IllegalArgumentException("Id \"" + sequenceFlow.id() + "\" is not unique");
        }

        if (!doesIdExist(sequenceFlow.sourceElementId())) {
            throw new IllegalArgumentException("Element with id \"" + sequenceFlow.sourceElementId() + "\" does not exist");
        }

        if (!doesIdExist(sequenceFlow.targetElementId())) {
            throw new IllegalArgumentException("Element with id \"" + sequenceFlow.targetElementId() + "\" does not exist");
        }

        Process process = modelInstance.getModelElementById(sequenceFlow.parentElementId());

        FlowNode sourceElement;
        if (sequenceFlow.sourceElementId() == null) {
            sourceElement = modelInstance.getModelElementsByType(StartEvent.class).iterator().next();
        } else {
            sourceElement = modelInstance.getModelElementById(sequenceFlow.sourceElementId());
        }

        FlowNode targetElement = modelInstance.getModelElementById(sequenceFlow.targetElementId());
        SequenceFlow sequenceFlowElement = createSequenceFlow(process, sequenceFlow.id(), sourceElement, targetElement);
        sequenceFlowElement.setAttributeValue("name", sequenceFlow.name());
    }

    public void removeElement(ElementToRemove elementToRemove) {
        if (!doesIdExist(elementToRemove.id())) {
            throw new IllegalArgumentException("Element with id \"" + elementToRemove.id() + "\" does not exist");
        }

        if (!doesIdExist(elementToRemove.parentId())) {
            throw new IllegalArgumentException("Element with id \"" + elementToRemove.parentId() + "\" does not exist");
        }

        ModelElementInstance parentElement = modelInstance.getModelElementById(elementToRemove.parentId());
        parentElement.removeChildElement(modelInstance.getModelElementById(elementToRemove.id()));
    }

    public boolean doesIdExist(String id) {
        return modelInstance.getModelElementById(id) != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BpmnModel) obj;
        return Objects.equals(this.modelInstance, that.modelInstance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelInstance);
    }

    @Override
    public String toString() {
        return "BpmnModel[" +
                "modelInstance=" + modelInstance + ']';
    }

    public BpmnModel getCopy() {
        return new BpmnModel(modelInstance);
    }
}
