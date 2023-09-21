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
                ChatMessage.systemMessage("""
                        Create BPMN models based on the business process description provided by the user. Follow the provided instructions."""),
                ChatMessage.systemMessage("Step 1 - The user will provide the process description in triple quotes (\"\"\"). First, extend the process description provided by the user - think about all relevant specifics and details. Focus on the happy path in this step. Enclose all your work for this step within triple quotes (\"\"\")."),
                ChatMessage.userMessage("\"\"\"" + prompt.content() + "\"\"\"")
        ));
        chatConversation.carryOutConversation(bpmnModel);

        chatConversation.addMessage(ChatMessage.systemMessage("Step 2 - Think about the possible problems that could arise in the process. When you are done, modify the description you provided accordingly. Enclose all your work for this step within triple quotes (\"\"\")."));
        chatConversation.carryOutConversation(bpmnModel);

        chatConversation.addMessage(ChatMessage.systemMessage("""
                Step 3 - Create the model based on your description by calling the provided functions. When creating the model use only the provided functions. Follow BPMN best practices:
                - Final states with different meaning should be modeled as separate and described.
                - Two end events in the same process should not have the same name.
                - Don’t use XOR gateways to merge alternative paths.
                - Don’t use gateway join into None end event.
                - A sequence flow may not connect to another sequence flow, only to an activity, gateway or event.
                """));
        chatConversation.carryOutConversation(bpmnModel);

        chatConversation.addMessage(ChatMessage.systemMessage("""
                Step 4 - Fix the errors that could be introduced in the model. Use only the provided functions. Examples of errors:
                - A gateway should have one incoming sequence flow, and two or more outgoing sequence flows.
                - Elements connected through a gateway should not also be connected directly.
                - Redundant paths between elements
                - Gateway without a name"""));
        chatConversation.carryOutConversation(bpmnModel);

        return BpmnFile.fromModel(bpmnModel);
    }

}
