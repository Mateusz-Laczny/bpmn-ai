package edu.agh.bpmnai.generator.bpmn;

import com.fasterxml.jackson.databind.JsonNode;
import edu.agh.bpmnai.generator.bpmn.model.*;
import edu.agh.bpmnai.generator.openai.ChatCallableInterface;
import edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory;
import edu.agh.bpmnai.generator.openai.model.ChatFunction;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import lombok.Getter;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class ChatModifiableBpmnModel implements ChatModifiableObject<BpmnModel> {
    private final BpmnModel modifiedModel;

    private final Function<JsonNode, Optional<ChatMessage>> addProcess = new BpmnModelFunctionCallExecutorTemplate<>(BpmnProcess.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnProcess callArgumentsPojo) {
            String processId = modifiedModel.addProcess(callArgumentsPojo);
            return Optional.of(ChatMessage.userMessage("Added process with id: \"" + processId + "\""));
        }
    };
    private final Function<JsonNode, Optional<ChatMessage>> addGateway = new BpmnModelFunctionCallExecutorTemplate<>(BpmnGateway.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnGateway callArgumentsPojo) {
            String gatewayId = modifiedModel.addGateway(callArgumentsPojo);
            return Optional.of(ChatMessage.userMessage("Added" + callArgumentsPojo.type().name().toLowerCase() + " gateway with id: \"" + gatewayId + "\""));
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addStartEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnStartEvent.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnStartEvent callArgumentsPojo) {
            String startEventId = modifiedModel.addStartEvent(callArgumentsPojo);
            return Optional.of(ChatMessage.userMessage("Added start event with id: \"" + startEventId + "\""));
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addEndEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnEndEvent.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnEndEvent callArgumentsPojo) {
            String endEventId = modifiedModel.addEndEvent(callArgumentsPojo);
            return Optional.of(ChatMessage.userMessage("Added end event with id: \"" + endEventId + "\""));
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addIntermediateCatchEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnIntermediateCatchEvent.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnIntermediateCatchEvent callArgumentsPojo) {
            String intermediateEventId = modifiedModel.addIntermediateCatchEvent(callArgumentsPojo);
            return Optional.of(ChatMessage.userMessage("Added intermediate catch event with id: \"" + intermediateEventId + "\""));
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addIntermediateThrowEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnIntermediateThrowEvent.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnIntermediateThrowEvent callArgumentsPojo) {
            String intermediateEventId = modifiedModel.addIntermediateThrowEvent(callArgumentsPojo);
            return Optional.of(ChatMessage.userMessage("Added intermediate throw event with id: \"" + intermediateEventId + "\""));
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addUserTask = new BpmnModelFunctionCallExecutorTemplate<>(BpmnUserTask.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnUserTask callArgumentsPojo) {
            String userTaskId = modifiedModel.addUserTask(callArgumentsPojo);
            return Optional.of(ChatMessage.userMessage("Added user task with id: \"" + userTaskId + "\""));
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addServiceTask = new BpmnModelFunctionCallExecutorTemplate<>(BpmnServiceTask.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnServiceTask callArgumentsPojo) {
            String serviceTaskId = modifiedModel.addServiceTask(callArgumentsPojo);
            return Optional.of(ChatMessage.userMessage("Added service task with id: \"" + serviceTaskId + "\""));
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addSequenceFlow = new BpmnModelFunctionCallExecutorTemplate<>(BpmnSequenceFlow.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnSequenceFlow callArgumentsPojo) {
            String sequenceFlowId = modifiedModel.addSequenceFlow(callArgumentsPojo);
            return Optional.of(ChatMessage.userMessage("Added sequence flow with id: \"" + sequenceFlowId + "\""));
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> removeElement = new BpmnModelFunctionCallExecutorTemplate<>(ElementToRemove.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(ElementToRemove callArgumentsPojo) {
            modifiedModel.removeElement(callArgumentsPojo);
            return Optional.empty();
        }
    };

    @Getter
    private final ChatCallableInterface chatCallableInterface = new ChatCallableInterface(Set.of(
            ChatFunction.builder()
                    .name("addProcess")
                    .description("Add a process to the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(BpmnProcess.class))
                    .executor(addProcess)
                    .build(),
            ChatFunction.builder()
                    .name("addGateway")
                    .description("Add an inclusive or exclusive gateway to the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(BpmnGateway.class))
                    .executor(addGateway)
                    .build(),
            ChatFunction.builder()
                    .name("addStartEvent")
                    .description("Add a start event to the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(BpmnStartEvent.class))
                    .executor(addStartEvent)
                    .build(),
            ChatFunction.builder()
                    .name("addEndEvent")
                    .description("Add an end event to the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(BpmnEndEvent.class))
                    .executor(addEndEvent)
                    .build(),
            ChatFunction.builder()
                    .name("addIntermediateCatchEvent")
                    .description("Add an intermediate catch event to the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(BpmnIntermediateCatchEvent.class))
                    .executor(addIntermediateCatchEvent)
                    .build(),
            ChatFunction.builder()
                    .name("addIntermediateThrowEvent")
                    .description("Add an intermediate throw event to the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(BpmnIntermediateThrowEvent.class))
                    .executor(addIntermediateThrowEvent)
                    .build(),
            ChatFunction.builder()
                    .name("addUserTask")
                    .description("Add a user task to the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(BpmnUserTask.class))
                    .executor(addUserTask)
                    .build(),
            ChatFunction.builder()
                    .name("addServiceTask")
                    .description("Add a service task to the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(BpmnServiceTask.class))
                    .executor(addServiceTask)
                    .build(),
            ChatFunction.builder()
                    .name("addSequenceFlow")
                    .description("Add a sequence flow between two elements of the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(BpmnSequenceFlow.class))
                    .executor(addSequenceFlow)
                    .build(),
            ChatFunction.builder()
                    .name("removeElement")
                    .description("Removes an element with a given id from the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(ElementToRemove.class))
                    .executor(removeElement)
                    .build()
    ));

    public ChatModifiableBpmnModel() {
        this.modifiedModel = new BpmnModel();
    }

    @Override
    public BpmnModel getObjectInstance() {
        return modifiedModel.getCopy();
    }
}
