package edu.agh.bpmnai.generator.bpmn.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import edu.agh.bpmnai.generator.bpmn.layouting.Point2d;
import edu.agh.bpmnai.generator.datatype.Result;
import jakarta.annotation.Nullable;
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static edu.agh.bpmnai.generator.bpmn.diagram.DiagramDimensions.*;
import static edu.agh.bpmnai.generator.bpmn.model.AddSequenceFlowError.*;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

@Slf4j
public final class BpmnModel {
    private final BpmnModelInstance modelInstance;

    private final Process defaultProcess;

    private final BpmnPlane diagramPlane;

    private final BiMap<String, String> idToModelFriendlyId;

    private BpmnModel(BpmnModel modelToCopy) {
        this.modelInstance = modelToCopy.modelInstance.clone();
        defaultProcess = modelInstance.getModelElementById(modelToCopy.defaultProcess.getId());
        diagramPlane = ((BpmnDiagram) modelInstance.getModelElementById("diagram")).getBpmnPlane();
        idToModelFriendlyId = HashBiMap.create(modelToCopy.idToModelFriendlyId);
    }

    public BpmnModel() {
        modelInstance = Bpmn.createEmptyModel();
        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setTargetNamespace("http://camunda.org/examples");
        modelInstance.setDefinitions(definitions);

        String processId = generateUniqueId();
        Process processElement = createElementWithParent(modelInstance.getDefinitions(), processId, Process.class);
        processElement.setAttributeValue("name", "default");
        defaultProcess = processElement;

        String diagramId = "diagram";
        BpmnDiagram diagram = createElementWithParent(definitions, diagramId, BpmnDiagram.class);
        diagramPlane = createElementWithParent(diagram, "id", BpmnPlane.class);
        diagram.setBpmnPlane(diagramPlane);

        String startEventId = addLabelledStartEvent("Start");
        idToModelFriendlyId = HashBiMap.create();
        idToModelFriendlyId.put(startEventId, "Start");

        Bpmn.validateModel(modelInstance);
    }

    public BpmnModel(String bpmnXml) {
        byte[] bytes = bpmnXml.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        modelInstance = Bpmn.readModelFromStream(inputStream);
        defaultProcess = modelInstance.getModelElementsByType(Process.class).iterator().next();
        diagramPlane = modelInstance.getModelElementsByType(BpmnDiagram.class).iterator().next().getBpmnPlane();
        idToModelFriendlyId = HashBiMap.create();
    }

    private static <T extends BpmnModelElementInstance> T createElementWithParent(
            BpmnModelElementInstance parentElement, String id, Class<T> elementClass
    ) {
        T element = parentElement.getModelInstance().newInstance(elementClass);
        element.setAttributeValue("id", id, true);
        parentElement.addChildElement(element);
        return element;
    }

    private static <T extends BpmnModelElementInstance> T createElementWithParent(
            BpmnModelElementInstance parentElement, Class<T> elementClass
    ) {
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

    public String addTask(String taskName, String modelFriendlyId) {
        log.trace("Adding task '{}' with model if '{}'", taskName, modelFriendlyId);
        String id = generateUniqueId();
        Task taskElement = createElementWithParent(defaultProcess, id, Task.class);
        taskElement.setAttributeValue("name", taskName);
        addTaskDiagramElement(taskElement);
        idToModelFriendlyId.put(id, modelFriendlyId);
        return id;
    }

    private void addTaskDiagramElement(Task taskElement) {
        String shapeId = generateUniqueId();
        BpmnShape shape = createElementWithParent(diagramPlane, shapeId, BpmnShape.class);
        shape.setBpmnElement(taskElement);
        Bounds bounds = createElementWithParent(shape, Bounds.class);
        shape.setBounds(bounds);
        bounds.setHeight(TASK_HEIGHT);
        bounds.setWidth(TASK_WIDTH);
        bounds.setX(0);
        bounds.setY(0);
    }

    private void addGatewayDiagramElement(Gateway gatewayElement) {
        String shapeId = generateUniqueId();
        BpmnShape shape = createElementWithParent(diagramPlane, shapeId, BpmnShape.class);
        shape.setBpmnElement(gatewayElement);
        Bounds bounds = createElementWithParent(shape, Bounds.class);
        bounds.setX(0);
        bounds.setY(0);
        bounds.setHeight(GATEWAY_DIAGONAL);
        bounds.setWidth(GATEWAY_DIAGONAL);
    }

    public String addGateway(BpmnGatewayType gatewayType, String name) {
        log.trace("Adding gateway of type '{}' with name '{}'", gatewayType, name);
        Gateway gatewayElement;
        final String id = generateUniqueId();
        switch (gatewayType) {
            case EXCLUSIVE -> gatewayElement = createElementWithParent(defaultProcess, id, ExclusiveGateway.class);
            case PARALLEL -> gatewayElement = createElementWithParent(defaultProcess, id, ParallelGateway.class);
            default -> throw new IllegalStateException("Unexpected gateway type value: " + gatewayType);
        }

        gatewayElement.setAttributeValue("name", name);
        idToModelFriendlyId.put(id, name);
        addGatewayDiagramElement(gatewayElement);
        return id;
    }

    public String addLabelledStartEvent(String label) {
        String id = generateUniqueId();
        StartEvent startEventElement = createElementWithParent(defaultProcess, id, StartEvent.class);
        startEventElement.setAttributeValue("name", label);
        addEventDiagramElement(startEventElement);
        return id;
    }

    private void addEventDiagramElement(Event element) {
        String shapeId = generateUniqueId();
        BpmnShape shape = createElementWithParent(diagramPlane, shapeId, BpmnShape.class);
        shape.setBpmnElement(element);
        Bounds bounds = createElementWithParent(shape, Bounds.class);
        bounds.setHeight(EVENT_DIAMETER);
        bounds.setWidth(EVENT_DIAMETER);
        bounds.setX(0);
        bounds.setY(0);
    }

    public String addEndEvent() {
        String id = generateUniqueId();
        EndEvent endEventElement = createElementWithParent(defaultProcess, id, EndEvent.class);
        addEventDiagramElement(endEventElement);
        return id;

    }

    private void addSequenceFlowDiagramElement(
            SequenceFlow sequenceFlowElement, FlowNode sourceElement, FlowNode targetElement
    ) {
        String edgeElementId = generateUniqueId();
        BpmnEdge edgeElement = createElementWithParent(diagramPlane, edgeElementId, BpmnEdge.class);
        edgeElement.setBpmnElement(sequenceFlowElement);
        Waypoint sourceWaypoint = createElementWithParent(edgeElement, Waypoint.class);
        Bounds sourceElementBoundsElement = ((BpmnShape) sourceElement.getDiagramElement()).getBounds();
        double sourceX = sourceElementBoundsElement.getX() + getElementDimensions(sourceElement.getId()).width();
        sourceWaypoint.setX(sourceX);
        double sourceY = sourceElementBoundsElement.getY() + (getElementDimensions(sourceElement.getId()).height() / 2);
        sourceWaypoint.setY(sourceY);

        Waypoint targetWaypoint = createElementWithParent(edgeElement, Waypoint.class);
        Bounds targetElementBoundsElement = ((BpmnShape) targetElement.getDiagramElement()).getBounds();
        targetWaypoint.setX(targetElementBoundsElement.getX());
        double targetY = targetElementBoundsElement.getY() + (getElementDimensions(targetElement.getId()).height() / 2);
        targetWaypoint.setY(targetY);
    }

    public Result<String, AddSequenceFlowError> addUnlabelledSequenceFlow(
            String sourceElementId,
            String targetElementId
    ) {
        log.trace("Adding unlabelled sequence flow from '{}' to '{}'", getModelFriendlyId(sourceElementId),
                  getModelFriendlyId(targetElementId)
        );

        FlowNode sourceElement = modelInstance.getModelElementById(sourceElementId);
        if (sourceElement == null) {
            return Result.error(SOURCE_ELEMENT_DOES_NOT_EXIST);
        }
        FlowNode targetElement = modelInstance.getModelElementById(targetElementId);
        if (targetElement == null) {
            return Result.error(TARGET_ELEMENT_DOES_NOT_EXIST);
        }

        if (areElementsDirectlyConnected(sourceElementId, targetElementId)) {
            return Result.error(ELEMENTS_ALREADY_CONNECTED);
        }

        String id = generateUniqueId();
        SequenceFlow sequenceFlowElement = createSequenceFlow(defaultProcess, id, sourceElement, targetElement);
        addSequenceFlowDiagramElement(sequenceFlowElement, sourceElement, targetElement);
        return Result.ok(id);
    }

    public Result<String, AddSequenceFlowError> addLabelledSequenceFlow(
            String sourceElementId,
            String targetElementId,
            String label
    ) {
        log.trace("Adding labelled sequence flow from '{}' to '{}'", getModelFriendlyId(sourceElementId),
                  getModelFriendlyId(targetElementId)
        );

        Result<String, AddSequenceFlowError> addSequenceFlowResult = addUnlabelledSequenceFlow(
                sourceElementId,
                targetElementId
        );

        if (addSequenceFlowResult.isError()) {
            return addSequenceFlowResult;
        }

        String sequenceFlowId = addSequenceFlowResult.getValue();
        SequenceFlow sequenceFlow = modelInstance.getModelElementById(sequenceFlowId);
        sequenceFlow.setAttributeValue("name", label);
        return addSequenceFlowResult;
    }

    public void removeElement(String idOfElementToRemove) {
        if (!doesIdExist(idOfElementToRemove)) {
            throw new IllegalArgumentException("Element with id \"" + idOfElementToRemove + "\" does not exist");
        }

        ModelElementInstance modelElement = modelInstance.getModelElementById(idOfElementToRemove);
        DiagramElement diagramElement = ((BaseElement) modelElement).getDiagramElement();
        diagramElement.getParentElement().removeChildElement(diagramElement);

        defaultProcess.removeChildElement(modelInstance.getModelElementById(idOfElementToRemove));
    }

    public Result<Void, RemoveActivityError> removeFlowNode(String flowNodeId) {
        ModelElementInstance elementInstance = modelInstance.getModelElementById(flowNodeId);
        if (elementInstance == null) {
            return Result.error(RemoveActivityError.ELEMENT_DOES_NOT_EXIST);
        }

        if (!(elementInstance instanceof FlowNode flowNodeInstance)) {
            return Result.error(RemoveActivityError.ELEMENT_IS_NOT_A_FLOW_NODE);
        }

        for (SequenceFlow incomingSequenceFlow : flowNodeInstance.getIncoming()) {
            removeElement(incomingSequenceFlow.getId());
        }

        for (SequenceFlow outgoingSequenceFlow : flowNodeInstance.getOutgoing()) {
            removeElement(outgoingSequenceFlow.getId());
        }

        removeElement(flowNodeId);

        return Result.ok(null);
    }

    public Optional<String> findElementByModelFriendlyId(String elementName) {
        return Optional.ofNullable(idToModelFriendlyId.inverse().get(elementName));
    }

    public LinkedHashSet<String> findPredecessors(String elementId) {
        FlowNode modelElementInstance = modelInstance.getModelElementById(elementId);
        return modelElementInstance.getIncoming().stream().map(sequenceFlow -> sequenceFlow.getSource().getId())
                .collect(toCollection(LinkedHashSet::new));
    }

    public LinkedHashSet<String> findSuccessors(String elementId) {
        FlowNode modelElementInstance = modelInstance.getModelElementById(elementId);
        return modelElementInstance.getOutgoing().stream().map(sequenceFlow -> sequenceFlow.getTarget().getId())
                .collect(toCollection(LinkedHashSet::new));
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

    public String getStartEvent() {
        return modelInstance.getModelElementsByType(StartEvent.class).iterator().next().getId();
    }

    public boolean doesIdExist(String id) {
        return modelInstance.getModelElementById(id) != null;
    }

    public void setPositionOfElement(String elementId, Point2d newPosition) {
        BaseElement targetElement = modelInstance.getModelElementById(elementId);
        Bounds targetElementBoundsElement = ((BpmnShape) targetElement.getDiagramElement()).getBounds();
        targetElementBoundsElement.setX(newPosition.x());
        targetElementBoundsElement.setY(newPosition.y());
        if (targetElement instanceof FlowNode targetFlowNode) {
            for (SequenceFlow incomingSequenceFlow : targetFlowNode.getIncoming()) {
                removeElement(incomingSequenceFlow.getId());
                if (incomingSequenceFlow.getName() != null) {
                    addLabelledSequenceFlow(
                            incomingSequenceFlow.getSource().getId(),
                            targetFlowNode.getId(),
                            incomingSequenceFlow.getName()
                    );
                } else {
                    addUnlabelledSequenceFlow(incomingSequenceFlow.getSource().getId(), targetFlowNode.getId());
                }
            }
        }
    }

    public Dimensions getElementDimensions(String elementId) {
        BaseElement targetElement = modelInstance.getModelElementById(elementId);
        Bounds targetElementBoundsElement = ((BpmnShape) targetElement.getDiagramElement()).getBounds();
        return new Dimensions(targetElementBoundsElement.getX(), targetElementBoundsElement.getY(),
                              targetElementBoundsElement.getWidth(), targetElementBoundsElement.getHeight()
        );
    }

    public Optional<BpmnElementType> getElementType(String elementId) {
        @Nullable ModelElementInstance modelElement = modelInstance.getModelElementById(elementId);
        if (modelElement == null) {
            return Optional.empty();
        }

        if (modelElement instanceof Activity) {
            return Optional.of(BpmnElementType.ACTIVITY);
        } else if (modelElement instanceof StartEvent) {
            return Optional.of(BpmnElementType.START_EVENT);
        } else if (modelElement instanceof EndEvent) {
            return Optional.of(BpmnElementType.END_EVENT);
        } else if (modelElement instanceof Gateway) {
            return Optional.of(BpmnElementType.GATEWAY);
        } else {
            return Optional.of(BpmnElementType.OTHER_ELEMENT);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (BpmnModel) obj;
        return Objects.equals(this.modelInstance, that.modelInstance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelInstance);
    }

    @Override
    public String toString() {
        return "BpmnModel[" + "modelInstance=" + modelInstance + ']';
    }

    public BpmnModel getCopy() {
        return new BpmnModel(this);
    }

    private String generateUniqueId() {
        boolean generatedUniqueId = false;
        String generatedId = null;
        while (!generatedUniqueId) {
            UUID uuid = UUID.randomUUID();
            // For some reason, id's in bpmn.io (the library currently used in the frontend) have to start with a
            // letter,
            // so we ensure that by concatenating a string to the beginning of the UUID
            generatedId = "id-" + uuid;
            if (!doesIdExist(generatedId)) {
                generatedUniqueId = true;
            }
        }

        return generatedId;
    }

    public boolean areElementsDirectlyConnected(String firstElementId, String secondElementId) {
        return findSuccessors(firstElementId).contains(secondElementId);
    }

    public String getModelFriendlyId(String elementId) {
        return idToModelFriendlyId.get(elementId);
    }

    public Result<Void, RemoveSequenceFlowError> removeSequenceFlow(String sourceElementId, String targetElementId) {
        ModelElementInstance sourceElement = modelInstance.getModelElementById(sourceElementId);
        if (sourceElement == null) {
            return Result.error(RemoveSequenceFlowError.SOURCE_ELEMENT_NOT_FOUND);
        }

        if (!(sourceElement instanceof FlowNode sourceFlowNode)) {
            return Result.error(RemoveSequenceFlowError.SOURCE_ELEMENT_NOT_FLOW_NODE);
        }

        ModelElementInstance targetElement = modelInstance.getModelElementById(targetElementId);
        if (targetElement == null) {
            return Result.error(RemoveSequenceFlowError.TARGET_ELEMENT_NOT_FOUND);
        }

        if (!(targetElement instanceof FlowNode targetFlowNode)) {
            return Result.error(RemoveSequenceFlowError.TARGET_ELEMENT_NOT_FLOW_NODE);
        }

        for (SequenceFlow sequenceFlow : sourceFlowNode.getOutgoing()) {
            if (sequenceFlow.getTarget().equals(targetFlowNode)) {
                removeElement(sequenceFlow.getId());
                return Result.ok(null);
            }
        }

        return Result.error(RemoveSequenceFlowError.ELEMENTS_NOT_CONNECTED);
    }

    public Set<String> findElementsOfType(BpmnElementType bpmnElementType) {
        Set<String> foundElements = new HashSet<>();
        for (BaseElement modelElementInstance :
                modelInstance.getModelElementsByType(BaseElement.class)) {
            String elementId = modelElementInstance.getAttributeValue("id");
            if (getElementType(elementId).orElseThrow() == bpmnElementType) {
                foundElements.add(elementId);
            }
        }

        return foundElements;
    }

    public Set<DirectedEdge> getOutgoingSequenceFlows(String elementId) {
        FlowNode elementNode = modelInstance.getModelElementById(elementId);
        return elementNode.getOutgoing().stream().map(sequenceFlow -> new DirectedEdge(
                sequenceFlow.getId(),
                elementId,
                sequenceFlow.getTarget().getId()
        )).collect(toSet());
    }

    public Set<DirectedEdge> getIncomingSequenceFlows(String elementId) {
        FlowNode elementNode = modelInstance.getModelElementById(elementId);
        return elementNode.getIncoming().stream().map(sequenceFlow -> new DirectedEdge(
                sequenceFlow.getId(),
                elementId,
                sequenceFlow.getTarget().getId()
        )).collect(toSet());
    }

    public Set<String> getFlowNodes() {
        return modelInstance.getModelElementsByType(FlowNode.class).stream().map(BaseElement::getId).collect(toSet());
    }

    public Set<DirectedEdge> getSequenceFlows() {
        return modelInstance.getModelElementsByType(SequenceFlow.class).stream().map(sequenceFlow -> new DirectedEdge(
                sequenceFlow.getId(),
                sequenceFlow.getSource().getId(),
                sequenceFlow.getTarget().getId()
        )).collect(toSet());
    }

    public void setWaypointsOfFlow(String flowId, List<Point2d> waypoints) {
        BpmnEdge edgeElement = ((SequenceFlow) modelInstance.getModelElementById(flowId)).getDiagramElement();
        edgeElement.getWaypoints().clear();

        for (Point2d waypointPosition : waypoints) {
            Waypoint waypoint = createElementWithParent(edgeElement, Waypoint.class);
            waypoint.setX(waypointPosition.x());
            waypoint.setY(waypointPosition.y());
        }
    }
}
