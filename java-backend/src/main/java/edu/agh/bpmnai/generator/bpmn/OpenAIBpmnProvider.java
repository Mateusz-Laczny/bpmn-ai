package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.ChatConversation;
import edu.agh.bpmnai.generator.TextPrompt;
import edu.agh.bpmnai.generator.bpmn.model.BpmnFile;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.OpenAIChatConversation;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAIBpmnProvider implements BpmnProvider {

    private static final float temperature = 0.4f;

    @Override
    public BpmnFile provideForTextPrompt(TextPrompt prompt) {
        BpmnModel bpmnModel = new BpmnModel();

        ChatConversation chatConversation = OpenAIChatConversation.emptyConversation(OpenAI.OpenAIModel.GPT_3_5_TURBO_16K, BpmnModel.callableInterface, temperature);
        chatConversation.addMessages(List.of(
                ChatMessage.systemMessage("You will be provided a business process description. First work out your own business process description based on the one provided by the user. Think about all relevant specifics and details. Focus on the happy path in this step. Enclose all your work for this step within triple quotes (\"\"\""),
                ChatMessage.userMessage(prompt.content())
        ));
        chatConversation.carryOutConversation(bpmnModel, false);

        ChatMessage modelResponse = chatConversation.getLastMessage();

        chatConversation.addMessage(ChatMessage.systemMessage("Think about the possible problems that could arise in the described process. When you are done, modify the description you provided accordingly. Enclose all your work for this step within triple quotes (\"\"\""));
        chatConversation.addMessage(ChatMessage.userMessage("Users description: \"\"\"" + prompt.content() + "\"\"\"\n" +
                "Your extended description: \n\n\n" + modelResponse.content() + "\"\"\"\n"));
        chatConversation.carryOutConversation(bpmnModel, false);

        chatConversation.addMessage(ChatMessage.systemMessage("""
                Create the model based on your description by calling the provided functions. When creating and manipulating the model only use the provided functions, do not provide the model source in your responses. Follow BPMN best practices:
                - Different outcomes of the process should be modelled with different end states.
                - End states with different meaning should be modeled as separate and described.
                - Two end events in the same process should not have the same name.
                - Don’t use XOR gateways to merge alternative paths.
                - Don’t use gateway join into None end event.
                - A sequence flow may not connect to another sequence flow, only to an activity, gateway or event.
                """));
        chatConversation.carryOutConversation(bpmnModel, true);

        return BpmnFile.fromModel(bpmnModel);
    }

}
