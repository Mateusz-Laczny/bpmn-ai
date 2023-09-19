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
                ChatMessage.userMessage(prompt.content() + ". Start with the happy path.")
        ));

        chatConversation.carryOutConversation(bpmnModel);

        if (chatConversation.getCurrentConversationStatus() == ConversationStatus.FINISHED) {
            chatConversation.addMessage(ChatMessage.userMessage("Now think about what problems may arise during the process and modify the model accordingly."));
            chatConversation.carryOutConversation(bpmnModel);
        }

        return BpmnFile.fromModel(bpmnModel);
    }

}
