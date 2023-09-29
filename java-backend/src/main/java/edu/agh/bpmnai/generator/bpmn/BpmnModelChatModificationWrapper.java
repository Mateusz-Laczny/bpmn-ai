package edu.agh.bpmnai.generator.bpmn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.Logging;
import edu.agh.bpmnai.generator.bpmn.model.*;
import edu.agh.bpmnai.generator.openai.ChatCallableInterface;
import edu.agh.bpmnai.generator.openai.model.ChatFunction;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import edu.agh.bpmnai.generator.openai.model.FunctionParameters;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class BpmnModelChatModificationWrapper {
    private static final ObjectMapper mapper = new ObjectMapper();
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

    private final Function<JsonNode, Optional<ChatMessage>> addIntermediateEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnIntermediateEvent.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnIntermediateEvent callArgumentsPojo) {
            String intermediateEventId = modifiedModel.addIntermediateEvent(callArgumentsPojo);
            return Optional.of(ChatMessage.userMessage("Added intermediate event with id: \"" + intermediateEventId + "\""));
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addMessageEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnMessageEvent.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnMessageEvent callArgumentsPojo) {
            String messageId = modifiedModel.addMessage(callArgumentsPojo.messageName());
            String messageEventId = modifiedModel.addMessageEvent(callArgumentsPojo.processId(), messageId);
            return Optional.of(ChatMessage.userMessage("Added message with id: \"" + messageId + "\", and message event with id: \"" + messageEventId + "\""));
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addSignalEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnSignalEvent.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnSignalEvent callArgumentsPojo) {
            String signalId = modifiedModel.addSignal(callArgumentsPojo.signalName());
            String signalEventId = modifiedModel.addSignalEvent(callArgumentsPojo.processId(), signalId);
            return Optional.of(ChatMessage.userMessage("Added signal with id: \"" + signalId + "\", and signal event with id: \"" + signalEventId + "\""));
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

    private final ChatCallableInterface callableInterface = new ChatCallableInterface(Set.of(
            ChatFunction.builder()
                    .name("addProcess")
                    .description("Add a process to the model")
                    .parameters(buildFunctionParameters(
                            new FunctionParameters()
                                    .addRequiredArgument("name", "string", "Name of this element")
                    ))
                    .executor(addProcess)
                    .build(),
            ChatFunction.builder()
                    .name("addGateway")
                    .description("Add an inclusive or exclusive gateway to the model")
                    .parameters(buildFunctionParameters(
                            new FunctionParameters()
                                    .addRequiredArgument("processId", "string", "Id of the process that this element is the member of")
                                    .addRequiredArgument("name", "string", "Name of this element")
                                    .addRequiredArgument("type", "string", "Id of the parent element of this element", List.of("exclusive", "inclusive"))
                    ))
                    .executor(addGateway)
                    .build(),
            ChatFunction.builder()
                    .name("addStartEvent")
                    .description("Add a start event to the model")
                    .parameters(buildFunctionParameters(
                            new FunctionParameters()
                                    .addRequiredArgument("processId", "string", "Id of the process that this element is the member of")
                                            .addOptionalArgument("name", "string", "Name of this element")
                            )
                    ).executor(addStartEvent)
                    .build(),
            ChatFunction.builder()
                    .name("addEndEvent")
                    .description("Add an end event to the model")
                    .parameters(buildFunctionParameters(
                            new FunctionParameters()
                                    .addRequiredArgument("processId", "string", "Id of the process that this element is the member of")
                                            .addRequiredArgument("name", "string", "Name of this element")
                            )
                    ).executor(addEndEvent)
                    .build(),
            ChatFunction.builder()
                    .name("addIntermediateEvent")
                    .description("Add an intermediate event to the model")
                    .parameters(buildFunctionParameters(
                            new FunctionParameters()
                                    .addRequiredArgument("processId", "string", "Id of the process that this element is the member of")
                                            .addRequiredArgument("name", "string", "Name of this element")
                                            .addRequiredArgument("isCatchEvent", "boolean", "Is it a catch event?")
                            )
                    ).executor(addIntermediateEvent)
                    .build(),
            ChatFunction.builder()
                    .name("addMessageEvent")
                    .description("Add a message event to the model")
                    .parameters(buildFunctionParameters(
                            new FunctionParameters()
                                    .addRequiredArgument("processId", "string", "Id of the process that this element is the member of")
                                            .addRequiredArgument("messageId", "string", "Id of the message element")
                                            .addRequiredArgument("messageName", "string", "Name of the message element element")
                            )
                    ).executor(addMessageEvent)
                    .build(),
            ChatFunction.builder()
                    .name("addSignalEvent")
                    .description("Add a signal event to the model")
                    .parameters(buildFunctionParameters(
                            new FunctionParameters()
                                    .addRequiredArgument("processId", "string", "Id of the process that this element is the member of")
                                            .addRequiredArgument("messageId", "string", "Id of the message element")
                                            .addRequiredArgument("messageName", "string", "Name of the message element element")
                            )
                    ).executor(addSignalEvent)
                    .build(),
            ChatFunction.builder()
                    .name("addUserTask")
                    .description("Add a user task to the model")
                    .parameters(buildFunctionParameters(
                            new FunctionParameters()
                                    .addRequiredArgument("processId", "string", "Id of the process that this element is the member of")
                                            .addRequiredArgument("name", "string", "Name of this element")
                                            .addOptionalArgument("assignee", "string", "Assignee to the user task")
                            )
                    ).executor(addUserTask)
                    .build(),
            ChatFunction.builder()
                    .name("addServiceTask")
                    .description("Add a service task to the model")
                    .parameters(buildFunctionParameters(
                            new FunctionParameters()
                                    .addRequiredArgument("processId", "string", "Id of the process that this element is the member of")
                                    .addRequiredArgument("name", "string", "Name of this element")
                    )).executor(addServiceTask)
                    .build(),
            ChatFunction.builder()
                    .name("addSequenceFlow")
                    .description("Add a sequence flow between two elements of the model")
                    .parameters(buildFunctionParameters(
                            new FunctionParameters()
                                    .addRequiredArgument("processId", "string", "Id of the process that this element is the member of")
                                    .addRequiredArgument("sourceElementId", "string", "Id of the source element of this sequence flow")
                                    .addRequiredArgument("targetElementId", "string", "Id of the source element of this sequence flow")
                                    .addOptionalArgument("label", "string", "Sequence flow label")

                    )).executor(addSequenceFlow)
                    .build(),
            ChatFunction.builder()
                    .name("removeElement")
                    .description("Removes an element with a given id from the model")
                    .parameters(buildFunctionParameters(
                            new FunctionParameters()
                                    .addRequiredArgument("id", "string", "Id of the element to remove. Must exist in the model")
                                    .addRequiredArgument("processId", "string", "Id of the parent element of this element")
                    )).executor(removeElement)
                    .build()
    ));

    public BpmnModelChatModificationWrapper() {
        this.modifiedModel = new BpmnModel();
    }

    private static JsonNode buildFunctionParameters(FunctionParameters functionParameters) {
        return mapper.valueToTree(functionParameters);
    }

    public ChatCallableInterface getCallableInterface() {
        return callableInterface;
    }

    public BpmnModel getModel() {
        return modifiedModel.getCopy();
    }

    private static abstract class BpmnModelFunctionCallExecutorTemplate<T> implements Function<JsonNode, Optional<ChatMessage>> {

        private final Class<T> callArgumentsClass;

        public BpmnModelFunctionCallExecutorTemplate(Class<T> callArgumentsClass) {
            this.callArgumentsClass = callArgumentsClass;
        }

        @Override
        public final Optional<ChatMessage> apply(JsonNode callArguments) {
            T callArgumentsPojo;
            try {
                callArgumentsPojo = parseCallArguments(callArguments);
            } catch (JsonProcessingException e) {
                Logging.logThrowable("Error when parsing function call arguments", e);
                ChatMessage errorMessage = ChatMessage.userMessage("The last function call parameters were invalid. Call the function again with proper arguments. Error: " + e.getMessage());
                return Optional.of(errorMessage);
            }

            Optional<ChatMessage> optionalArgumentVerificationError = verifyCallArguments(callArgumentsPojo);
            if (optionalArgumentVerificationError.isPresent()) {
                return optionalArgumentVerificationError;
            }

            Optional<ChatMessage> messageAfterCallExecution;
            try {
                messageAfterCallExecution = executeFunctionCall(callArgumentsPojo);
            } catch (IllegalArgumentException e) {
                Logging.logThrowable("Error when executing function call", e);
                return Optional.of(ChatMessage.userMessage(e.getMessage()));
            }
            return messageAfterCallExecution;
        }

        protected abstract Optional<ChatMessage> executeFunctionCall(T callArgumentsPojo);

        protected Optional<ChatMessage> verifyCallArguments(T callArgumentsPojo) {
            return Optional.empty();
        }

        protected T parseCallArguments(JsonNode callArguments) throws JsonProcessingException {
            return mapper.readValue(callArguments.asText(), callArgumentsClass);
        }
    }
}
