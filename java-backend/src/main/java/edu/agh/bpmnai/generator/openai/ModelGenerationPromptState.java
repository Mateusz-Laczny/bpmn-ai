package edu.agh.bpmnai.generator.openai;

import edu.agh.bpmnai.generator.openai.model.ChatMessage;

import java.util.List;

public class ModelGenerationPromptState implements PromptingState {

    @Override
    public List<ChatMessage> getPromptForCurrentState() {
        return List.of(
                ChatMessage.systemMessage("""
                        Create the model based on your description by calling the provided functions. When creating and manipulating the model only use the provided functions, do not provide the model source in your responses.
                                        
                        Start by defining required processes.
                                        
                        Follow BPMN best practices:
                        - Different outcomes of the process should be modelled with different end states.
                        - End states with different meaning should be modeled as separate and described.
                        - Two end events in the same process should not have the same name.
                        - Don’t use XOR gateways to merge alternative paths.
                        - A sequence flow may not connect to another sequence flow, only to an activity, gateway or event.
                        """),
                ChatMessage.systemMessage("""
                        Description of available BPMN elements:
                        Pool - Represents a collection of processes and their coordination. Consists of one or more lanes.
                        Collapsed pool - Hides a process whose details are unknown or unimportant.
                        Lane - Indicates the responsibility for a task. Named after the person or entity responsible.
                        Subprocess - Encapsulates a complex process. Must have its own start and end events. No sequence flows can cross its boundaries.
                        Activities:
                        Task - A process activity.
                        Manual task - A task performed manually without software.
                        User task - A semi-automated task performed using software.
                        Service task - A task completely automated.
                        Message task - Involves sending or receiving a message, and can be interrupted.
                        Gateways - Indicate possible flows in the process. Not decisions; the decision is made by the activity preceding the gateway.
                        XOR gateway - Exclusive choice where only one path can be taken.
                        AND gateway - Represents tasks that can be done in parallel.
                        Flows - Indicate the process flow.
                        Sequence flows - Show the order of activities in a process. Cannot cross sub-process or pool boundaries.
                        Message flow - Show communication between participants. Cannot connect objects within the same pool.
                        Events - Something that can happen during a process.
                        Start event - Marks the beginning of a process.
                        End event - Marks a possible end result of a process.
                        Catch event - Triggered after a defined trigger event and influences the process flow.
                        Throw event - Self-triggered event.
                        Boundary events - Connect tasks. Cancels the attached task when it occurs, and executes the connected task instead. Must have one outgoing sequence flow with no incoming flows""")
        );
    }

    @Override
    public boolean hasNextState() {
        return false;
    }

    @Override
    public boolean isFunctionCallingStep() {
        return true;
    }

    @Override
    public PromptingState getNextState(String previousPromptResult) {
        throw new IllegalStateException("Current state is the final state");
    }
}
