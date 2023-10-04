package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.Logging;
import edu.agh.bpmnai.generator.TextPrompt;
import edu.agh.bpmnai.generator.bpmn.layouting.BpmnSemanticLayouting;
import edu.agh.bpmnai.generator.bpmn.model.BpmnFile;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.openai.ChatModifiableObject;
import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.OpenAIChatSession;
import edu.agh.bpmnai.generator.openai.OpenAIChatSessionFactory;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAIBpmnProvider implements BpmnProvider {

    private static final float temperature = 0.4f;
    private static final OpenAI.OpenAIModel aiModel = OpenAI.OpenAIModel.GPT_3_5_TURBO_16K;

    private final OpenAIChatSessionFactory chatSessionFactory;

    private final BpmnSemanticLayouting layouting;

    @Autowired
    public OpenAIBpmnProvider(OpenAIChatSessionFactory chatSessionFactory, BpmnSemanticLayouting layouting) {
        this.chatSessionFactory = chatSessionFactory;
        this.layouting = layouting;
    }

    @Override
    public BpmnFile provideForTextPrompt(TextPrompt userDescription) {
        ChatModifiableObject<BpmnModel> chatModifiableBpmnModel = new ChatModifiableBpmnModel();
        OpenAIChatSession chatSession = chatSessionFactory.createNewSession(aiModel, temperature);
        List<ChatMessage> prompt = List.of(
                ChatMessage.systemMessage("You will be provided a business process description. First work out your own business process description based on the one provided by the user. Think about all relevant specifics and details. Focus on the happy path in this step. Enclose all your work for this step within triple quotes (\"\"\""),
                ChatMessage.userMessage(userDescription.content())
        );

        ChatMessage responseMessage = chatSession.generateResponseFromPrompt(prompt);

        prompt = List.of(
                ChatMessage.systemMessage("Think about the possible problems that could arise in the described process. When you are done, modify the description you provided accordingly. Enclose all your work for this step within triple quotes (\"\"\""),
                ChatMessage.userMessage("Users description: \"\"\"" + userDescription.content() + "\"\"\"\n" +
                        "Your extended description: \n\n\n" + responseMessage.getContent() + "\"\"\"\n")
        );

        chatSession.generateResponseFromPrompt(prompt);

        chatSession.addMessage(ChatMessage.systemMessage("""
                Create the model based on your description by calling the provided functions. When creating and manipulating the model only use the provided functions, do not provide the model source in your responses.
                                
                Start by defining required processes.
                                
                Follow BPMN best practices:
                - Different outcomes of the process should be modelled with different end states.
                - End states with different meaning should be modeled as separate and described.
                - Two end events in the same process should not have the same name.
                - Don’t use XOR gateways to merge alternative paths.
                - A sequence flow may not connect to another sequence flow, only to an activity, gateway or event.
                """));
        chatSession.addMessage(ChatMessage.systemMessage("""
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
                Boundary events - Connect tasks. Cancels the attached task when it occurs, and executes the connected task instead. Must have one outgoing sequence flow with no incoming flows"""));

        chatSession.generateResponseFromPrompt(prompt, chatModifiableBpmnModel.getChatCallableInterface());

        Logging.logInfoMessage("Finished model generation");

        BpmnModel bpmnModel = chatModifiableBpmnModel.getObjectInstance();
        layouting.layoutModel(bpmnModel);

        return BpmnFile.fromModel(bpmnModel);
    }

}
