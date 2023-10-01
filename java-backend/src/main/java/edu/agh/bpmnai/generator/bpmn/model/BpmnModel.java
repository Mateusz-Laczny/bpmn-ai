package edu.agh.bpmnai.generator.bpmn.model;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.Objects;
import java.util.UUID;

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

    private SequenceFlow createSequenceFlow(Process process, String sequenceFlowId, FlowNode from, FlowNode to) {
        SequenceFlow sequenceFlow = createElementWithParent(process, sequenceFlowId, SequenceFlow.class);
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

    public String addUserTask(BpmnUserTask userTask) {
        Process process = modelInstance.getModelElementById(userTask.processId());
        String id = generateUniqueId();
        UserTask userTaskElement = createElementWithParent(process, id, UserTask.class);
        userTaskElement.setAttributeValue("name", userTask.name());
        userTaskElement.setCamundaAssignee(userTask.assignee());
        return id;
    }

    public String addServiceTask(BpmnServiceTask serviceTask) {
        Process process = modelInstance.getModelElementById(serviceTask.processId());
        String id = generateUniqueId();
        ServiceTask serviceTaskElement = createElementWithParent(process, id, ServiceTask.class);
        serviceTaskElement.setAttributeValue("name", serviceTask.name());
        return id;
    }

    public String addProcess(BpmnProcess process) {
        String id = generateUniqueId();
        Process processElement = createElementWithParent(modelInstance.getDefinitions(), id, Process.class);
        processElement.setAttributeValue("name", process.name());
        return id;
    }

    public String addGateway(BpmnGateway gateway) {
        Process process = modelInstance.getModelElementById(gateway.processId());
        Gateway gatewayElement;
        final String id = generateUniqueId();
        switch (gateway.type()) {
            case EXCLUSIVE -> gatewayElement = createElementWithParent(process, id, ExclusiveGateway.class);
            case INCLUSIVE -> gatewayElement = createElementWithParent(process, id, InclusiveGateway.class);
            default -> throw new IllegalStateException("Unexpected gateway type value: " + gateway.type());
        }

        gatewayElement.setAttributeValue("name", gateway.name());
        return id;
    }

    public String addStartEvent(BpmnStartEvent startEvent) {
        Process process = modelInstance.getModelElementById(startEvent.processId());
        String id = generateUniqueId();
        StartEvent startEventElement = createElementWithParent(process, id, StartEvent.class);
        startEventElement.setAttributeValue("name", startEvent.name());
        return id;
    }

    public String addEndEvent(BpmnEndEvent endEvent) {
        Process process = modelInstance.getModelElementById(endEvent.processId());
        String id = generateUniqueId();
        EndEvent endEventElement = createElementWithParent(process, id, EndEvent.class);
        endEventElement.setAttributeValue("name", endEvent.name());
        return id;
    }

    public String addIntermediateCatchEvent(BpmnIntermediateCatchEvent intermediateCatchEvent) {
        Process process = modelInstance.getModelElementById(intermediateCatchEvent.processId());
        String id = generateUniqueId();
        IntermediateCatchEvent catchEventElement = createElementWithParent(process, id, IntermediateCatchEvent.class);
        catchEventElement.setAttributeValue("name", intermediateCatchEvent.name());

        String eventId = generateUniqueId();
        switch (intermediateCatchEvent.eventType()) {
            case MESSAGE -> createElementWithParent(catchEventElement, eventId, MessageEventDefinition.class);
            case TIMER -> createElementWithParent(catchEventElement, eventId, TimerEventDefinition.class);
            case CONDITIONAL -> createElementWithParent(catchEventElement, eventId, ConditionalEventDefinition.class);
            case LINK -> createElementWithParent(catchEventElement, eventId, LinkEventDefinition.class);
            case SIGNAL -> createElementWithParent(catchEventElement, eventId, SignalEventDefinition.class);
        }

        return id;
    }

    public String addIntermediateThrowEvent(BpmnIntermediateThrowEvent intermediateThrowEvent) {
        Process process = modelInstance.getModelElementById(intermediateThrowEvent.processId());
        String id = generateUniqueId();
        IntermediateThrowEvent catchEventElement = createElementWithParent(process, id, IntermediateThrowEvent.class);
        catchEventElement.setAttributeValue("name", intermediateThrowEvent.name());

        String eventId = generateUniqueId();
        switch (intermediateThrowEvent.eventType()) {
            case EMPTY -> {
            }
            case MESSAGE -> createElementWithParent(catchEventElement, eventId, MessageEventDefinition.class);
            case ESCALATION -> createElementWithParent(catchEventElement, eventId, EscalationEventDefinition.class);
            case LINK -> createElementWithParent(catchEventElement, eventId, LinkEventDefinition.class);
            case COMPENSATION -> createElementWithParent(catchEventElement, eventId, CompensateEventDefinition.class);
            case SIGNAL -> createElementWithParent(catchEventElement, eventId, SignalEventDefinition.class);
        }

        return id;
    }

    public String addSequenceFlow(BpmnSequenceFlow sequenceFlow) {
        if (!doesIdExist(sequenceFlow.sourceElementId())) {
            throw new IllegalArgumentException("Element with id \"" + sequenceFlow.sourceElementId() + "\" does not exist");
        }

        if (!doesIdExist(sequenceFlow.targetElementId())) {
            throw new IllegalArgumentException("Element with id \"" + sequenceFlow.targetElementId() + "\" does not exist");
        }

        Process process = modelInstance.getModelElementById(sequenceFlow.processId());

        FlowNode sourceElement;
        if (sequenceFlow.sourceElementId() == null) {
            sourceElement = modelInstance.getModelElementsByType(StartEvent.class).iterator().next();
        } else {
            sourceElement = modelInstance.getModelElementById(sequenceFlow.sourceElementId());
        }

        FlowNode targetElement = modelInstance.getModelElementById(sequenceFlow.targetElementId());
        String id = generateUniqueId();
        SequenceFlow sequenceFlowElement = createSequenceFlow(process, id, sourceElement, targetElement);
        sequenceFlowElement.setAttributeValue("name", sequenceFlow.name());
        return id;
    }

    public void removeElement(ElementToRemove elementToRemove) {
        if (!doesIdExist(elementToRemove.id())) {
            throw new IllegalArgumentException("Element with id \"" + elementToRemove.id() + "\" does not exist");
        }

        if (!doesIdExist(elementToRemove.processId())) {
            throw new IllegalArgumentException("Element with id \"" + elementToRemove.processId() + "\" does not exist");
        }

        ModelElementInstance parentElement = modelInstance.getModelElementById(elementToRemove.processId());
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

    private String generateUniqueId() {
        boolean generatedUniqueId = false;
        String generatedId = null;
        while (!generatedUniqueId) {
            UUID uuid = UUID.randomUUID();
            // For some reason, id's in bpmn.io (the library currently used in the frontend) have to start with a letter,
            // so we ensure that by concatenating a string to the beginning of the UUID
            generatedId = "id-" + uuid;
            if (!doesIdExist(generatedId)) {
                generatedUniqueId = true;
            }
        }

        return generatedId;
    }
}
