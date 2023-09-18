package edu.agh.bpmnai.generator.bpmn.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.model.ChatFunction;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class BpmnModel {
    public static final int functionDescriptionsTokens;
    public static List<ChatFunction> functionsDescriptions = List.of(
            new ChatFunction(
                    "addUserTask",
                    "Add a user task to the model",
                    Map.of(
                            "type",
                            "object",
                            "properties",
                            Map.of(
                                    "id",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the element. Must be globally unique"
                                    ),
                                    "processId",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the parent process element of this element"
                                    ),
                                    "name",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Name of the element"
                                    ),
                                    "assignee",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Assignee to the user task"
                                    )
                            ),
                            "required",
                            List.of("id", "processId", "name")
                    )
            ),
            new ChatFunction(
                    "addServiceTask",
                    "Add a service task to the model",
                    Map.of(
                            "type",
                            "object",
                            "properties",
                            Map.of(
                                    "id",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the element. Must be globally unique"
                                    ),
                                    "processId",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the parent process element of this element"
                                    ),
                                    "name",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Name of the element"
                                    )
                            ),
                            "required",
                            List.of("id", "processId", "name")
                    )
            ),
            new ChatFunction(
                    "addProcess",
                    "Add a process to the model",
                    Map.of(
                            "type",
                            "object",
                            "properties",
                            Map.of(
                                    "id",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the element. Must be globally unique"
                                    ),
                                    "name",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Name of the element"
                                    )
                            ),
                            "required",
                            List.of("id", "name")
                    )),
            new ChatFunction(
                    "addGateway",
                    "Add a gateway to the model",
                    Map.of(
                            "type",
                            "object",
                            "properties",
                            Map.of(
                                    "id",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the element. Must be globally unique"
                                    ),
                                    "processId",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the parent process element of this element"
                                    ),
                                    "name",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Name of the element"
                                    ),
                                    "type",
                                    Map.of(
                                            "type",
                                            "string",

                                            "enum",
                                            List.of("exclusive", "inclusive"),

                                            "description",
                                            "Type of the gateway"
                                    )
                            ),
                            "required",
                            List.of("id", "processId", "name", "type")
                    )),
            new ChatFunction(
                    "addStartEvent",
                    "Add a start event to the model",
                    Map.of(
                            "type",
                            "object",
                            "properties",
                            Map.of(
                                    "id",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the element. Must be globally unique"
                                    ),
                                    "processId",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the parent process element of this element"
                                    )
                            ),
                            "required",
                            List.of("id", "processId")
                    )),
            new ChatFunction(
                    "addEndEvent",
                    "Add an end event to the model",
                    Map.of(
                            "type",
                            "object",
                            "properties",
                            Map.of(
                                    "id",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the element. Must be globally unique"
                                    ),
                                    "processId",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the parent process element of this element"
                                    )
                            ),
                            "required",
                            List.of("id", "processId")
                    )),
            new ChatFunction(
                    "addIntermediateEvent",
                    "Add an intermediate event to the model",
                    Map.of(
                            "type",
                            "object",
                            "properties",
                            Map.of(
                                    "id",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the element. Must be globally unique"
                                    ),
                                    "processId",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the parent process element of this element"
                                    ),
                                    "name",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Name of the element"
                                    ),
                                    "catchEvent",
                                    Map.of(
                                            "type",
                                            "boolean",
                                            "description",
                                            "Is the event catch event"
                                    )
                            ),
                            "required",
                            List.of("id", "processId", "name", "catchEvent")
                    )),
            new ChatFunction(
                    "addMessageEvent",
                    "Add a message event to the model",
                    Map.of(
                            "type",
                            "object",
                            "properties",
                            Map.of(
                                    "parentElementId",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the parent element of this event"
                                    ),

                                    "messageId",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the message element"
                                    ),
                                    "messageName",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Name of the message element"
                                    ),
                                    "eventId",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the event element"
                                    )
                            ),
                            "required",
                            List.of("parentElementId", "messageId", "messageName", "eventId")
                    )),
            new ChatFunction(
                    "addSignalEvent",
                    "Add an intermediate event to the model",
                    Map.of(
                            "type",
                            "object",
                            "properties",
                            Map.of(
                                    "parentElementId",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the parent element of this event"
                                    ),

                                    "messageId",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the message element"
                                    ),
                                    "messageName",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Name of the message element"
                                    ),
                                    "eventId",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the event element"
                                    )
                            ),
                            "required",
                            List.of("parentElementId", "messageId", "messageName", "eventId")
                    )),
            new ChatFunction(
                    "addSequenceFlow",
                    "Add a sequence flow between two elements to the model",
                    Map.of(
                            "type",
                            "object",
                            "properties",
                            Map.of(
                                    "id",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the element. Must be globally unique"
                                    ),

                                    "parentElementId",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the parent element of this event"
                                    ),

                                    "sourceRef",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the source element of this sequence flow"
                                    ),
                                    "targetRef",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the target element of this sequence flow"
                                    ),
                                    "name",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Name pof the element"
                                    )
                            ),
                            "required",
                            List.of("id", "parentElementId", "sorceRef", "targetRef")
                    )
            ),
            new ChatFunction(
                    "removeElement",
                    "Removes an element with a given id from the model",
                    Map.of(
                            "type",
                            "object",
                            "properties",
                            Map.of(
                                    "id",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the element. Must be globally unique"
                                    ),
                                    "parentId",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Id of the element's parent"
                                    )
                            ),
                            "required",
                            List.of("id", "parentId")
                    )
            )
    );

    static {
        try {
            functionDescriptionsTokens = OpenAI.getApproximateNumberOfTokens(new ObjectMapper().writeValueAsString(functionsDescriptions));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private final BpmnModelInstance modelInstance;

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
        return Bpmn.convertToString(modelInstance);
    }

    public void addUserTask(BpmnUserTask userTask) {
        Process process = modelInstance.getModelElementById(userTask.processId());
        UserTask camudaUserTask = createElementWithParent(process, userTask.id(), UserTask.class);
        camudaUserTask.setName(userTask.name());
        camudaUserTask.setCamundaAssignee(userTask.assignee());
    }

    public void addServiceTask(BpmnServiceTask serviceTask) {
        Process process = modelInstance.getModelElementById(serviceTask.processId());
        ServiceTask camudaServiceTask = createElementWithParent(process, serviceTask.id(), ServiceTask.class);
        camudaServiceTask.setName(serviceTask.name());
    }

    public void addProcess(BpmnProcess process) {
        Process camudaProcess = createElementWithParent(modelInstance.getDefinitions(), process.id(), Process.class);
        camudaProcess.setName(process.name());
    }

    public void addGateway(BpmnGateway gateway) {
        Process process = modelInstance.getModelElementById(gateway.processId());
        switch (gateway.type()) {
            case EXCLUSIVE -> createElementWithParent(process, gateway.id(), ExclusiveGateway.class);
            case INCLUSIVE -> createElementWithParent(process, gateway.id(), InclusiveGateway.class);
        }
    }

    public void addStartEvent(BpmnStartEvent startEvent) {
        Process process = modelInstance.getModelElementById(startEvent.processId());
        createElementWithParent(process, startEvent.id(), StartEvent.class);
    }

    public void addEndEvent(BpmnEndEvent endEvent) {
        Process process = modelInstance.getModelElementById(endEvent.processId());
        createElementWithParent(process, endEvent.id(), EndEvent.class);
    }

    public void addIntermediateEvent(BpmnIntermediateEvent intermediateEvent) {
        Process process = modelInstance.getModelElementById(intermediateEvent.processId());
        if (intermediateEvent.catchEvent()) {
            createElementWithParent(process, intermediateEvent.id(), IntermediateCatchEvent.class);
        } else {
            createElementWithParent(process, intermediateEvent.id(), IntermediateThrowEvent.class);
        }
    }

    public void addMessageEvent(BpmnMessageEvent messageEvent) {
        Message message = createElementWithParent(modelInstance.getDefinitions(), messageEvent.messageId(), Message.class);
        message.setName(messageEvent.messageName());
        MessageEventDefinition messageEventDefinition = createElementWithParent(modelInstance.getModelElementById(messageEvent.parentElementId()), messageEvent.eventId(), MessageEventDefinition.class);
        messageEventDefinition.setMessage(message);
    }

    public void addSignalEvent(BpmnSignalEvent signalEvent) {
        Signal signal = createElementWithParent(modelInstance.getDefinitions(), signalEvent.signalId(), Signal.class);
        signal.setName(signalEvent.signalName());
        SignalEventDefinition signalEventDefinition = createElementWithParent(modelInstance.getModelElementById(signalEvent.parentElementId()), signalEvent.signalEventId(), SignalEventDefinition.class);
        signalEventDefinition.setSignal(signal);
    }

    public void addSequenceFlow(BpmnSequenceFlow sequenceFlow) {
        Process process = modelInstance.getModelElementById(sequenceFlow.parentElementId());

        FlowNode sourceElement;
        if (sequenceFlow.sourceRef() == null) {
            sourceElement = modelInstance.getModelElementsByType(StartEvent.class).iterator().next();
        } else {
            sourceElement = modelInstance.getModelElementById(sequenceFlow.sourceRef());
        }

        FlowNode targetElement = modelInstance.getModelElementById(sequenceFlow.targetRef());
        SequenceFlow camudaSequenceFlow = createSequenceFlow(process, sequenceFlow.id(), sourceElement, targetElement);
        camudaSequenceFlow.setName(sequenceFlow.name());
    }

    public void removeElement(String id, String parentId) {
        ModelElementInstance parentElement = modelInstance.getModelElementById(parentId);
        parentElement.removeChildElement(modelInstance.getModelElementById(id));
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

}
