package edu.agh.bpmnai.generator.bpmn.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.ElementToRemove;
import edu.agh.bpmnai.generator.openai.model.ChatFunction;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class BpmnModel {

    private static final ObjectMapper mapper = new ObjectMapper();
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
                                    ),
                                    "name",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Name of the start event"
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
                                    ),
                                    "name",
                                    Map.of(
                                            "type",
                                            "string",
                                            "description",
                                            "Name of the end event"
                                    )
                            ),
                            "required",
                            List.of("id", "processId", "name")
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

    public Optional<FunctionCallError> parseModelFunctionCall(ChatMessage responseMessage) {
        String functionName = responseMessage.function_call().get("name").asText();
        JsonNode functionArguments = responseMessage.function_call().get("arguments");
        switch (functionName) {
            case "addProcess" -> {
                BpmnProcess process;
                try {
                    process = mapper.readValue(functionArguments.asText(), BpmnProcess.class);
                } catch (JsonProcessingException e) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.INVALID_PARAMETERS, functionArguments));
                }

                if (doesIdExist(process.id())) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.NON_UNIQUE_ID, functionArguments));
                }

                addProcess(process);
            }
            case "addStartEvent" -> {
                BpmnStartEvent startEvent;
                try {
                    startEvent = mapper.readValue(functionArguments.asText(), BpmnStartEvent.class);
                } catch (JsonProcessingException e) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.INVALID_PARAMETERS, functionArguments));
                }

                if (doesIdExist(startEvent.id())) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.NON_UNIQUE_ID, functionArguments));
                }

                addStartEvent(startEvent);
            }
            case "addEndEvent" -> {
                BpmnEndEvent endEvent;
                try {
                    endEvent = mapper.readValue(functionArguments.asText(), BpmnEndEvent.class);
                } catch (JsonProcessingException e) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.INVALID_PARAMETERS, functionArguments));
                }

                if (doesIdExist(endEvent.id())) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.NON_UNIQUE_ID, functionArguments));
                }


                addEndEvent(endEvent);
            }
            case "addUserTask" -> {
                BpmnUserTask userTask;
                try {
                    userTask = mapper.readValue(functionArguments.asText(), BpmnUserTask.class);
                } catch (JsonProcessingException e) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.INVALID_PARAMETERS, functionArguments));
                }

                if (doesIdExist(userTask.id())) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.NON_UNIQUE_ID, functionArguments));
                }

                addUserTask(userTask);
            }
            case "addServiceTask" -> {
                BpmnServiceTask serviceTask;
                try {
                    serviceTask = mapper.readValue(functionArguments.asText(), BpmnServiceTask.class);
                } catch (JsonProcessingException e) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.INVALID_PARAMETERS, functionArguments));
                }

                if (doesIdExist(serviceTask.id())) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.NON_UNIQUE_ID, functionArguments));
                }


                addServiceTask(serviceTask);
            }
            case "addGateway" -> {
                BpmnGateway gateway;
                try {
                    gateway = mapper.readValue(functionArguments.asText(), BpmnGateway.class);
                } catch (JsonProcessingException e) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.INVALID_PARAMETERS, functionArguments));
                }

                if (doesIdExist(gateway.id())) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.NON_UNIQUE_ID, functionArguments));
                }

                addGateway(gateway);
            }
            case "addSequenceFlow" -> {
                BpmnSequenceFlow sequenceFlow;
                try {
                    sequenceFlow = mapper.readValue(functionArguments.asText(), BpmnSequenceFlow.class);
                } catch (JsonProcessingException e) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.INVALID_PARAMETERS, functionArguments));
                }

                if (doesIdExist(sequenceFlow.id())) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.NON_UNIQUE_ID, functionArguments));
                } else if (sequenceFlow.parentElementId() == null || sequenceFlow.id() == null || sequenceFlow.sourceRef() == null || sequenceFlow.targetRef() == null) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.MISSING_PARAMETERS, functionArguments));
                } else if (!doesIdExist(sequenceFlow.sourceRef()) || !doesIdExist(sequenceFlow.targetRef())) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.ELEMENT_NOT_FOUND, functionArguments));
                }

                addSequenceFlow(sequenceFlow);
            }
            case "addIntermediateEvent" -> {
                BpmnIntermediateEvent intermediateEvent;
                try {
                    intermediateEvent = mapper.readValue(functionArguments.asText(), BpmnIntermediateEvent.class);
                } catch (JsonProcessingException e) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.INVALID_PARAMETERS, functionArguments));
                }

                if (doesIdExist(intermediateEvent.id())) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.NON_UNIQUE_ID, functionArguments));
                }

                addIntermediateEvent(intermediateEvent);
            }
            case "removeElement" -> {
                ElementToRemove elementToRemove;
                try {
                    elementToRemove = mapper.readValue(functionArguments.asText(), ElementToRemove.class);
                } catch (JsonProcessingException e) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.INVALID_PARAMETERS, functionArguments));
                }

                if (!doesIdExist(elementToRemove.id()) || !doesIdExist(elementToRemove.parentId())) {
                    return Optional.of(new FunctionCallError(FunctionCallErrorType.ELEMENT_NOT_FOUND, functionArguments));
                }

                removeElement(elementToRemove.id(), elementToRemove.parentId());
            }
            default -> throw new IllegalStateException("Unrecognised function name:" + functionName);
        }

        return Optional.empty();
    }

    public String asXmlString() {
        Bpmn.validateModel(modelInstance);
        return Bpmn.convertToString(modelInstance);
    }

    public void addUserTask(BpmnUserTask userTask) {
        Process process = modelInstance.getModelElementById(userTask.processId());
        UserTask userTaskElement = createElementWithParent(process, userTask.id(), UserTask.class);
        userTaskElement.setAttributeValue("name", userTask.name());
        userTaskElement.setCamundaAssignee(userTask.assignee());
    }

    public void addServiceTask(BpmnServiceTask serviceTask) {
        Process process = modelInstance.getModelElementById(serviceTask.processId());
        ServiceTask serviceTaskElement = createElementWithParent(process, serviceTask.id(), ServiceTask.class);
        serviceTaskElement.setAttributeValue("name", serviceTask.name());
    }

    public void addProcess(BpmnProcess process) {
        Process processElement = createElementWithParent(modelInstance.getDefinitions(), process.id(), Process.class);
        processElement.setAttributeValue("name", process.name());
    }

    public void addGateway(BpmnGateway gateway) {
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
        Process process = modelInstance.getModelElementById(startEvent.processId());
        StartEvent startEventElement = createElementWithParent(process, startEvent.id(), StartEvent.class);
        startEventElement.setAttributeValue("name", startEvent.name());
    }

    public void addEndEvent(BpmnEndEvent endEvent) {
        Process process = modelInstance.getModelElementById(endEvent.processId());
        EndEvent endEventElement = createElementWithParent(process, endEvent.id(), EndEvent.class);
        endEventElement.setAttributeValue("name", endEvent.name());
    }

    public void addIntermediateEvent(BpmnIntermediateEvent intermediateEvent) {
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
        SequenceFlow sequenceFlowElement = createSequenceFlow(process, sequenceFlow.id(), sourceElement, targetElement);
        sequenceFlowElement.setAttributeValue("name", sequenceFlow.name());
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

    public enum FunctionCallErrorType {
        NON_UNIQUE_ID,
        INVALID_PARAMETERS,
        MISSING_PARAMETERS,
        ELEMENT_NOT_FOUND,
    }

    public record FunctionCallError(FunctionCallErrorType errorType, JsonNode functionCallAsJson) {
    }
}
