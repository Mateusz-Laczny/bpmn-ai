package edu.agh.bpmnai.generator.bpmn.model;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;
import org.camunda.bpm.model.bpmn.instance.di.DiagramElement;
import org.camunda.bpm.model.bpmn.instance.di.Waypoint;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.*;

import static java.util.stream.Collectors.toSet;

@Slf4j
public final class BpmnModel {
    private final BpmnModelInstance modelInstance;

    private final String idOfDefaultProcess;

    private final BpmnPlane diagramPlane;

    private final Map<String, String> aliases;

    private BpmnModel(BpmnModelInstance modelInstanceToCopy, String idOfDefaultProcess) {
        this.modelInstance = modelInstanceToCopy.clone();
        this.idOfDefaultProcess = idOfDefaultProcess;
        diagramPlane = ((BpmnDiagram) modelInstance.getModelElementById("diagram")).getBpmnPlane();
        aliases = new HashMap<>();
    }

    public BpmnModel() {
        modelInstance = Bpmn.createEmptyModel();
        aliases = new HashMap<>();
        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setTargetNamespace("http://camunda.org/examples");
        modelInstance.setDefinitions(definitions);

        idOfDefaultProcess = addProcess(new BpmnProcess("default"));

        String diagramId = "diagram";
        BpmnDiagram diagram = createElementWithParent(definitions, diagramId, BpmnDiagram.class);
        diagramPlane = createElementWithParent(diagram, "id", BpmnPlane.class);
        diagram.setBpmnPlane(diagramPlane);

        String startEventId = addStartEvent(new BpmnStartEvent(idOfDefaultProcess, ""));
        aliases.put("Start", startEventId);

        Bpmn.validateModel(modelInstance);
    }

    private static <T extends BpmnModelElementInstance> T createElementWithParent(BpmnModelElementInstance parentElement, String id, Class<T> elementClass) {
        T element = parentElement.getModelInstance().newInstance(elementClass);
        element.setAttributeValue("id", id, true);
        parentElement.addChildElement(element);
        return element;
    }

    private static <T extends BpmnModelElementInstance> T createElementWithParent(BpmnModelElementInstance parentElement, Class<T> elementClass) {
        T element = parentElement.getModelInstance().newInstance(elementClass);
        parentElement.addChildElement(element);
        return element;
    }

    private SequenceFlow createSequenceFlow(Process process, String sequenceFlowId, FlowNode from, FlowNode to) {
        SequenceFlow sequenceFlow = createElementWithParent(process, sequenceFlowId, SequenceFlow.class);
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

    public String addTask(String taskName) {
        Process process = modelInstance.getModelElementById(idOfDefaultProcess);
        String id = generateUniqueId();
        Task userTaskElement = createElementWithParent(process, id, Task.class);
        userTaskElement.setAttributeValue("name", taskName);
        addTaskDiagramElement(userTaskElement);
        return id;
    }

    private void addTaskDiagramElement(Task taskElement) {
        String shapeId = generateUniqueId();
        BpmnShape shape = createElementWithParent(diagramPlane, shapeId, BpmnShape.class);
        shape.setBpmnElement(taskElement);
        Bounds bounds = createElementWithParent(shape, Bounds.class);
        shape.setBounds(bounds);
        bounds.setHeight(68);
        bounds.setWidth(83);
        bounds.setX(0);
        bounds.setY(0);
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
        addGatewayDiagramElement(gatewayElement);
        return id;
    }

    private void addGatewayDiagramElement(Gateway gatewayElement) {
        String shapeId = generateUniqueId();
        BpmnShape shape = createElementWithParent(diagramPlane, shapeId, BpmnShape.class);
        shape.setBpmnElement(gatewayElement);
        Bounds bounds = createElementWithParent(shape, Bounds.class);
        bounds.setX(0);
        bounds.setY(0);
        bounds.setHeight(42);
        bounds.setWidth(42);
    }

    public String addGateway(BpmnGatewayType gatewayType) {
        return addGateway(new BpmnGateway(idOfDefaultProcess, null, gatewayType));
    }

    public String addStartEvent(BpmnStartEvent startEvent) {
        Process process = modelInstance.getModelElementById(startEvent.processId());
        String id = generateUniqueId();
        StartEvent startEventElement = createElementWithParent(process, id, StartEvent.class);
        startEventElement.setAttributeValue("name", startEvent.name());
        addStartEventDiagramElement(startEventElement);
        return id;
    }

    private void addStartEventDiagramElement(BaseElement element) {
        String shapeId = generateUniqueId();
        BpmnShape shape = createElementWithParent(diagramPlane, shapeId, BpmnShape.class);
        shape.setBpmnElement(element);
        Bounds bounds = createElementWithParent(shape, Bounds.class);
        bounds.setHeight(30);
        bounds.setWidth(30);
        bounds.setX(0);
        bounds.setY(0);
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
        addSequenceFlowDiagramElement(sequenceFlowElement, sourceElement, targetElement);
        return id;
    }

    private void addSequenceFlowDiagramElement(SequenceFlow sequenceFlowElement, FlowNode sourceElement, FlowNode targetElement) {
        String edgeElementId = generateUniqueId();
        BpmnEdge edgeElement = createElementWithParent(diagramPlane, edgeElementId, BpmnEdge.class);
        edgeElement.setBpmnElement(sequenceFlowElement);
        Waypoint sourceWaypoint = createElementWithParent(edgeElement, Waypoint.class);
        Bounds sourceElementBoundsElement = ((BpmnShape) sourceElement.getDiagramElement()).getBounds();
        sourceWaypoint.setX(sourceElementBoundsElement.getX());
        sourceWaypoint.setY(sourceElementBoundsElement.getY());

        Waypoint targetWaypoint = createElementWithParent(edgeElement, Waypoint.class);
        Bounds targetElementBoundsElement = ((BpmnShape) targetElement.getDiagramElement()).getBounds();
        targetWaypoint.setX(targetElementBoundsElement.getX());
        targetWaypoint.setY(targetElementBoundsElement.getY());
    }

    public String addUnlabelledSequenceFlow(String sourceElementId, String targetElementId) {
        return addSequenceFlow(new BpmnSequenceFlow(idOfDefaultProcess, sourceElementId, targetElementId, null));
    }

    public String addLabelledSequenceFlow(String sourceElementId, String targetElementId, String label) {
        return addSequenceFlow(new BpmnSequenceFlow(idOfDefaultProcess, sourceElementId, targetElementId, label));
    }

    public void removeElement(ElementToRemove elementToRemove) {
        if (!doesIdExist(elementToRemove.id())) {
            throw new IllegalArgumentException("Element with id \"" + elementToRemove.id() + "\" does not exist");
        }

        if (!doesIdExist(elementToRemove.processId())) {
            throw new IllegalArgumentException("Element with id \"" + elementToRemove.processId() + "\" does not exist");
        }

        ModelElementInstance modelElement = modelInstance.getModelElementById(elementToRemove.id());
        DiagramElement diagramElement = ((BaseElement) modelElement).getDiagramElement();
        diagramElement.getParentElement().removeChildElement(diagramElement);

        ModelElementInstance parentElement = modelInstance.getModelElementById(elementToRemove.processId());
        parentElement.removeChildElement(modelInstance.getModelElementById(elementToRemove.id()));
    }

    public void removeElement(String idOfElementToRemove) {
        removeElement(new ElementToRemove(idOfElementToRemove, idOfDefaultProcess));
    }

    public void cutOutElement(String elementId) {
        FlowNode element = modelInstance.getModelElementById(elementId);
        Set<FlowNode> predecessors = element.getIncoming().stream().map(SequenceFlow::getSource).collect(toSet());
        Set<FlowNode> successors = element.getOutgoing().stream().map(SequenceFlow::getTarget).collect(toSet());
        for (SequenceFlow incomingSequenceFlow : element.getIncoming()) {
            removeElement(incomingSequenceFlow.getId());
        }

        for (SequenceFlow outgoingSequenceFlow : element.getOutgoing()) {
            removeElement(outgoingSequenceFlow.getId());
        }

        for (FlowNode predecessor : predecessors) {
            for (FlowNode successor : successors) {
                addUnlabelledSequenceFlow(predecessor.getId(), successor.getId());
            }
        }

        removeElement(elementId);
    }

    public Optional<String> findTaskIdByName(String taskName) {
        if (aliases.containsKey(taskName)) {
            return Optional.of(aliases.get(taskName));
        }
        Optional<Task> task = getTaskByName(taskName);
        return task.map(BaseElement::getId);
    }

    private Optional<Task> getTaskByName(String taskName) {
        for (Task task : modelInstance.getModelElementsByType(Task.class)) {
            if (task.getName().equals(taskName)) {
                return Optional.of(task);
            }
        }
        return Optional.empty();
    }

    public Set<String> findPredecessors(String elementId) {
        FlowNode modelElementInstance = modelInstance.getModelElementById(elementId);
        return modelElementInstance.getIncoming().stream().map(sequenceFlow -> sequenceFlow.getSource().getId()).collect(toSet());
    }

    public Set<String> findSuccessors(String elementId) {
        FlowNode modelElementInstance = modelInstance.getModelElementById(elementId);
        return modelElementInstance.getOutgoing().stream().map(sequenceFlow -> sequenceFlow.getTarget().getId()).collect(toSet());
    }

    public void clearSuccessors(String elementId) {
        FlowNode modelElementInstance = modelInstance.getModelElementById(elementId);
        Collection<SequenceFlow> outgoingSequenceFlows = modelElementInstance.getOutgoing();
        for (SequenceFlow outgoingSequenceFlow : outgoingSequenceFlows) {
            removeElement(outgoingSequenceFlow.getId());
            modelElementInstance.getOutgoing().remove(outgoingSequenceFlow);
            outgoingSequenceFlow.getTarget().getIncoming().remove(outgoingSequenceFlow);
        }
    }

    public Set<String> findStartEvents() {
        return modelInstance.getModelElementsByType(StartEvent.class).stream().map(BaseElement::getId).collect(toSet());
    }

    public boolean doesIdExist(String id) {
        return modelInstance.getModelElementById(id) != null;
    }

    public void setPositionOfElement(String elementId, double newX, double newY) {
        BaseElement targetElement = modelInstance.getModelElementById(elementId);
        Bounds targetElementBoundsElement = ((BpmnShape) targetElement.getDiagramElement()).getBounds();
        targetElementBoundsElement.setX(newX);
        targetElementBoundsElement.setY(newY);
        if (targetElement instanceof FlowNode targetFlowNode) {
            for (SequenceFlow incomingSequenceFlow : targetFlowNode.getIncoming()) {
                removeElement(incomingSequenceFlow.getId());
                addSequenceFlow(new BpmnSequenceFlow(idOfDefaultProcess, incomingSequenceFlow.getSource().getId(), targetFlowNode.getId(), incomingSequenceFlow.getName()));
            }
        }
    }

    public Dimensions getElementDimensions(String elementId) {
        System.out.println(asXmlString());
        BaseElement targetElement = modelInstance.getModelElementById(elementId);
        Bounds targetElementBoundsElement = ((BpmnShape) targetElement.getDiagramElement()).getBounds();
        return new Dimensions(targetElementBoundsElement.getX(), targetElementBoundsElement.getY(), targetElementBoundsElement.getWidth(), targetElementBoundsElement.getHeight());
    }

    public void setAlias(String elementId, String alias) {
        aliases.put(elementId, alias);
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
        return new BpmnModel(modelInstance, idOfDefaultProcess);
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
