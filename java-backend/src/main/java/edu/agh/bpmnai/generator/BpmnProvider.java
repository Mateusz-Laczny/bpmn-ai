package edu.agh.bpmnai.generator;

import edu.agh.bpmnai.generator.bpmn.model.BpmnFile;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BpmnProvider {

    private static final float temperature = 0.4f;

    public BpmnFile provideForTextPrompt(TextPrompt prompt) {
        BpmnModel bpmnModel = new BpmnModel();

        ChatConversation chatConversation = OpenAIChatConversation.emptyConversationWith(OpenAI.OpenAIModel.GPT_3_5_TURBO_16K, temperature);
        chatConversation.addMessages(List.of(
                ChatMessage.systemMessage("When creating a BPMN model for the user, use only the provided functions"),
                ChatMessage.systemMessage("""
                        Try to follow BPMN best practices:
                        - Final states with different meaning should be modeled as separate and described.
                        - Two end events in the same process should not have the same name.
                        - Don’t use XOR gateways to merge alternative paths.
                        - Don’t use gateway join into None end event.
                        - A sequence flow may not connect to another sequence flow, only to an activity,
                        gateway or event."""),
                ChatMessage.userMessage(prompt.content() + ". Start with the happy path. Try to be as specific as possible.")
        ));

        chatConversation.carryOutConversation(bpmnModel);

        if (chatConversation.getCurrentConversationStatus() == ConversationStatus.FINISHED) {
            chatConversation.addMessage(ChatMessage.userMessage("Now think about what problems may arise during the process and modify the model accordingly."));
            chatConversation.carryOutConversation(bpmnModel);
        }

        if (chatConversation.getCurrentConversationStatus() == ConversationStatus.FINISHED) {
            chatConversation.addMessage(ChatMessage.userMessage("Remove unnecessary elements from the model. Try to make it as succinct as possible. Use the provided \"removeElement\" function to remove redundant elements."));
            chatConversation.carryOutConversation(bpmnModel);
        }

        if (chatConversation.getCurrentConversationStatus() == ConversationStatus.FINISHED) {
            chatConversation.addMessage(ChatMessage.userMessage("""
                    Fix the errors that could be introduced in the model. Examples of possible errors:
                    - A gateway should have one incoming sequence flow, and two or more outgoing sequence flows.
                    - Elements connected through a gateway should not also be connected directly.
                    - Redundant paths between elements"""));
            chatConversation.carryOutConversation(bpmnModel);
        }

        return BpmnFile.fromModel(bpmnModel);
    }

}
