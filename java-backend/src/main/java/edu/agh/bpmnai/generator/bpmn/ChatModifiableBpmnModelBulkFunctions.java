package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.bpmn.model.*;
import edu.agh.bpmnai.generator.openai.ChatCallableInterface;
import edu.agh.bpmnai.generator.openai.ChatModifiableObject;
import edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory;
import edu.agh.bpmnai.generator.openai.model.ChatFunction;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import lombok.Getter;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;


public class ChatModifiableBpmnModelBulkFunctions implements ChatModifiableObject<BpmnModel> {
    private final BpmnModel modifiedModel;

    private final Function<String, Optional<ChatMessage>> addProcesses = new BpmnModelFunctionCallExecutorTemplate<>(Processes.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(Processes callArgumentsPojo) {
            StringBuilder builder = new StringBuilder();
            builder.append("Added following processes :\n");
            for (BpmnProcess process : callArgumentsPojo.processes()) {
                String processId = modifiedModel.addProcess(process);
                builder.append(process.name()).append(" with id \"").append(processId).append('"');
            }

            return Optional.of(ChatMessage.userMessage(builder.toString()));
        }
    };
    private final Function<String, Optional<ChatMessage>> addGateways = new BpmnModelFunctionCallExecutorTemplate<>(Gateways.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(Gateways callArgumentsPojo) {
            StringBuilder builder = new StringBuilder();
            builder.append("Added following gateways:\n");
            for (BpmnGateway gateway : callArgumentsPojo.gateways()) {
                String gatewayId = modifiedModel.addGateway(gateway);
                builder.append(gateway.name()).append(" with id \"").append(gatewayId).append('"').append(" of type").append('"').append(gateway.type()).append('"');
            }

            return Optional.of(ChatMessage.userMessage(builder.toString()));
        }
    };

    private final Function<String, Optional<ChatMessage>> addStartEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnStartEvent.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnStartEvent callArgumentsPojo) {
            String startEventId = modifiedModel.addStartEvent(callArgumentsPojo);
            return Optional.of(ChatMessage.userMessage("Added start event with id: \"" + startEventId + "\""));
        }
    };

    private final Function<String, Optional<ChatMessage>> addEndEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnEndEvent.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnEndEvent callArgumentsPojo) {
            String endEventId = modifiedModel.addEndEvent(callArgumentsPojo);
            return Optional.of(ChatMessage.userMessage("Added end event with id: \"" + endEventId + "\""));
        }
    };

    private final Function<String, Optional<ChatMessage>> addIntermediateCatchEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnIntermediateCatchEvent.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnIntermediateCatchEvent callArgumentsPojo) {
            String intermediateEventId = modifiedModel.addIntermediateCatchEvent(callArgumentsPojo);
            return Optional.of(ChatMessage.userMessage("Added intermediate catch event with id: \"" + intermediateEventId + "\""));
        }
    };

    private final Function<String, Optional<ChatMessage>> addIntermediateThrowEvent = new BpmnModelFunctionCallExecutorTemplate<>(BpmnIntermediateThrowEvent.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(BpmnIntermediateThrowEvent callArgumentsPojo) {
            String intermediateEventId = modifiedModel.addIntermediateThrowEvent(callArgumentsPojo);
            return Optional.of(ChatMessage.userMessage("Added intermediate throw event with id: \"" + intermediateEventId + "\""));
        }
    };

    private final Function<String, Optional<ChatMessage>> addUserTasks = new BpmnModelFunctionCallExecutorTemplate<>(UserTasks.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(UserTasks callArgumentsPojo) {
            StringBuilder builder = new StringBuilder();
            builder.append("Added following user tasks:\n");
            for (BpmnUserTask userTask : callArgumentsPojo.userTasks()) {
                String userTaskId = modifiedModel.addUserTask(userTask);
                builder.append(userTask.name()).append(" with id \"").append(userTaskId).append('"');
            }

            return Optional.of(ChatMessage.userMessage(builder.toString()));
        }
    };

    private final Function<String, Optional<ChatMessage>> addServiceTasks = new BpmnModelFunctionCallExecutorTemplate<>(ServiceTasks.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(ServiceTasks callArgumentsPojo) {
            StringBuilder builder = new StringBuilder();
            builder.append("Added following service tasks:\n");
            for (BpmnServiceTask serviceTask : callArgumentsPojo.serviceTasks()) {
                String serviceTaskId = modifiedModel.addServiceTask(serviceTask);
                builder.append(serviceTask.name()).append(" with id \"").append(serviceTaskId).append('"');
            }

            return Optional.of(ChatMessage.userMessage(builder.toString()));
        }
    };

    private final Function<String, Optional<ChatMessage>> addSequenceFlows = new BpmnModelFunctionCallExecutorTemplate<>(SequenceFlows.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(SequenceFlows callArgumentsPojo) {
            StringBuilder builder = new StringBuilder();
            builder.append("Added following sequence flows:\n");
            for (BpmnSequenceFlow sequenceFlow : callArgumentsPojo.sequenceFlows()) {
                String sequenceFlowId = modifiedModel.addSequenceFlow(sequenceFlow);
                builder
                        .append(sequenceFlow.name())
                        .append(" with id ")
                        .append('"')
                        .append(sequenceFlowId)
                        .append('"')
                        .append(" between elements with ids ")
                        .append('"')
                        .append(sequenceFlow.sourceElementId())
                        .append('"')
                        .append(" and ")
                        .append("'")
                        .append(sequenceFlow.targetElementId())
                        .append('"');
            }

            return Optional.of(ChatMessage.userMessage(builder.toString()));
        }
    };

    private final Function<String, Optional<ChatMessage>> removeElement = new BpmnModelFunctionCallExecutorTemplate<>(ElementToRemove.class) {
        @Override
        protected Optional<ChatMessage> executeFunctionCall(ElementToRemove callArgumentsPojo) {
            modifiedModel.removeElement(callArgumentsPojo);
            return Optional.empty();
        }
    };

    @Getter
    private final ChatCallableInterface chatCallableInterface = new ChatCallableInterface(Set.of(
            ChatFunction.builder()
                    .name("addProcesses")
                    .description("Add a set of processes to the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(Processes.class))
                    .executor(addProcesses)
                    .build(),
            ChatFunction.builder()
                    .name("addGateway")
                    .description("Add a set of inclusive or exclusive gateways to the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(Gateways.class))
                    .executor(addGateways)
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
                    .name("addUserTasks")
                    .description("Add a set of user tasks to the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(UserTasks.class))
                    .executor(addUserTasks)
                    .build(),
            ChatFunction.builder()
                    .name("addServiceTasks")
                    .description("Add a set of service task to the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(ServiceTasks.class))
                    .executor(addServiceTasks)
                    .build(),
            ChatFunction.builder()
                    .name("addSequenceFlows")
                    .description("Add a set of sequence flows to the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(SequenceFlows.class))
                    .executor(addSequenceFlows)
                    .build(),
            ChatFunction.builder()
                    .name("removeElement")
                    .description("Removes an element with a given id from the model")
                    .parameters(OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto(ElementToRemove.class))
                    .executor(removeElement)
                    .build()
    ));

    public ChatModifiableBpmnModelBulkFunctions() {
        this.modifiedModel = new BpmnModel();
    }

    @Override
    public BpmnModel getObjectInstance() {
        return modifiedModel.getCopy();
    }

    private record Processes(Set<BpmnProcess> processes) {
    }

    private record Gateways(Set<BpmnGateway> gateways) {
    }

    private record UserTasks(Set<BpmnUserTask> userTasks) {
    }

    private record ServiceTasks(Set<BpmnServiceTask> serviceTasks) {
    }

    private record SequenceFlows(Set<BpmnSequenceFlow> sequenceFlows) {
    }
}
