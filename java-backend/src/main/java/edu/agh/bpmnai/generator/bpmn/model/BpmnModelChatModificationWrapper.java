package edu.agh.bpmnai.generator.bpmn.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.Logging;
import edu.agh.bpmnai.generator.bpmn.ElementToRemove;
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
        protected void executeFunctionCall(BpmnProcess callArgumentsPojo) {
            modifiedModel.addProcess(callArgumentsPojo);
        }
    };
    private final Function<JsonNode, Optional<ChatMessage>> addGateway = new BpmnModelFunctionCallExecutorTemplate<>(BpmnGateway.class) {
        @Override
        protected void executeFunctionCall(BpmnGateway callArgumentsPojo) {
            modifiedModel.addGateway(callArgumentsPojo);
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addStartEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnStartEvent.class) {
        @Override
        protected void executeFunctionCall(BpmnStartEvent callArgumentsPojo) {
            modifiedModel.addStartEvent(callArgumentsPojo);
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addEndEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnEndEvent.class) {
        @Override
        protected void executeFunctionCall(BpmnEndEvent callArgumentsPojo) {
            modifiedModel.addEndEvent(callArgumentsPojo);
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addIntermediateEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnIntermediateEvent.class) {
        @Override
        protected void executeFunctionCall(BpmnIntermediateEvent callArgumentsPojo) {
            modifiedModel.addIntermediateEvent(callArgumentsPojo);
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addMessageEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnMessageEvent.class) {
        @Override
        protected void executeFunctionCall(BpmnMessageEvent callArgumentsPojo) {
            modifiedModel.addMessageEvent(callArgumentsPojo);
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addSignalEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnSignalEvent.class) {
        @Override
        protected void executeFunctionCall(BpmnSignalEvent callArgumentsPojo) {
            modifiedModel.addSignalEvent(callArgumentsPojo);
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addUserTask = new BpmnModelFunctionCallExecutorTemplate<>(BpmnUserTask.class) {
        @Override
        protected void executeFunctionCall(BpmnUserTask callArgumentsPojo) {
            modifiedModel.addUserTask(callArgumentsPojo);
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addServiceTask = new BpmnModelFunctionCallExecutorTemplate<>(BpmnServiceTask.class) {
        @Override
        protected void executeFunctionCall(BpmnServiceTask callArgumentsPojo) {
            modifiedModel.addServiceTask(callArgumentsPojo);
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> addSequenceFlow = new BpmnModelFunctionCallExecutorTemplate<>(BpmnSequenceFlow.class) {
        @Override
        protected void executeFunctionCall(BpmnSequenceFlow callArgumentsPojo) {
            modifiedModel.addSequenceFlow(callArgumentsPojo);
        }
    };

    private final Function<JsonNode, Optional<ChatMessage>> removeElement = new BpmnModelFunctionCallExecutorTemplate<>(ElementToRemove.class) {
        @Override
        protected void executeFunctionCall(ElementToRemove callArgumentsPojo) {
            modifiedModel.removeElement(callArgumentsPojo);
        }
    };

    private final ChatCallableInterface callableInterface = new ChatCallableInterface(Set.of(
            ChatFunction.builder()
                    .name("addProcess")
                    .description("Add a process to the model")
                    .parameters(buildFunctionParameters(
                            new FunctionParameters()
                                    .addRequiredArgument("id", "string", "Id of the element. Must be globally unique")
                                    .addRequiredArgument("name", "string", "Name of this element")
                    ))
                    .executor(addProcess)
                    .build(),
            ChatFunction.builder()
                    .name("addGateway")
                    .description("Add a process to the model")
                    .parameters(buildFunctionParameters(
                            new FunctionParameters()
                                    .addRequiredArgument("id", "string", "Id of the element. Must be globally unique")
                                    .addRequiredArgument("parentId", "string", "Id of the parent element of this element")
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
                                            .addRequiredArgument("id", "string", "Id of the element. Must be globally unique")
                                            .addRequiredArgument("parentId", "string", "Id of the parent element of this element")
                                            .addOptionalArgument("name", "string", "Name of this element")
                            )
                    ).executor(addStartEvent)
                    .build(),
            ChatFunction.builder()
                    .name("addEndEvent")
                    .description("Add an end event to the model")
                    .parameters(buildFunctionParameters(
                                    new FunctionParameters()
                                            .addRequiredArgument("id", "string", "Id of the element. Must be globally unique")
                                            .addRequiredArgument("parentId", "string", "Id of the parent element of this element")
                                            .addRequiredArgument("name", "string", "Name of this element")
                            )
                    ).executor(addEndEvent)
                    .build(),
            ChatFunction.builder()
                    .name("addIntermediateEvent")
                    .description("Add an intermediate event to the model")
                    .parameters(buildFunctionParameters(
                                    new FunctionParameters()
                                            .addRequiredArgument("id", "string", "Id of the element. Must be globally unique")
                                            .addRequiredArgument("parentId", "string", "Id of the parent element of this element")
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
                                            .addRequiredArgument("id", "string", "Id of the element. Must be globally unique")
                                            .addRequiredArgument("parentId", "string", "Id of the parent element of this element")
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
                                            .addRequiredArgument("id", "string", "Id of the element. Must be globally unique")
                                            .addRequiredArgument("parentId", "string", "Id of the parent element of this element")
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
                                            .addRequiredArgument("id", "string", "Id of the element. Must be globally unique")
                                            .addRequiredArgument("parentId", "string", "Id of the parent element of this element")
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
                                    .addRequiredArgument("id", "string", "Id of the element. Must be globally unique")
                                    .addRequiredArgument("parentId", "string", "Id of the parent element of this element")
                                    .addRequiredArgument("name", "string", "Name of this element")
                    )).executor(addServiceTask)
                    .build(),
            ChatFunction.builder()
                    .name("addSequenceFlow")
                    .description("Add a sequence flow between two elements of the model")
                    .parameters(buildFunctionParameters(
                            new FunctionParameters()
                                    .addRequiredArgument("id", "string", "Id of the element. Must be globally unique")
                                    .addRequiredArgument("parentId", "string", "Id of the parent element of this element")
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
                                    .addRequiredArgument("id", "string", "Id of the element. Must be globally unique")
                                    .addRequiredArgument("parentId", "string", "Id of the parent element of this element")
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

            try {
                executeFunctionCall(callArgumentsPojo);
            } catch (IllegalArgumentException e) {
                Logging.logThrowable("Error when executing function call", e);
                return Optional.of(ChatMessage.userMessage(e.getMessage()));
            }
            return Optional.empty();
        }

        protected abstract void executeFunctionCall(T callArgumentsPojo);

        protected Optional<ChatMessage> verifyCallArguments(T callArgumentsPojo) {
            return Optional.empty();
        }

        protected T parseCallArguments(JsonNode callArguments) throws JsonProcessingException {
            return mapper.treeToValue(callArguments, callArgumentsClass);
        }
    }
}
