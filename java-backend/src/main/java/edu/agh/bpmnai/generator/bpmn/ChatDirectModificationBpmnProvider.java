package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.Logging;
import edu.agh.bpmnai.generator.TextPrompt;
import edu.agh.bpmnai.generator.bpmn.layouting.BpmnSemanticLayouting;
import edu.agh.bpmnai.generator.bpmn.model.BpmnFile;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.openai.*;
import edu.agh.bpmnai.generator.openai.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatDirectModificationBpmnProvider implements BpmnProvider {

    private static final float temperature = 0.4f;
    private static final OpenAI.OpenAIModel aiModel = OpenAI.OpenAIModel.GPT_3_5_TURBO_16K;

    private final OpenAIChatSessionFactory chatSessionFactory;

    private final BpmnSemanticLayouting layouting;

    @Autowired
    public ChatDirectModificationBpmnProvider(OpenAIChatSessionFactory chatSessionFactory, BpmnSemanticLayouting layouting) {
        this.chatSessionFactory = chatSessionFactory;
        this.layouting = layouting;
    }

    @Override
    public BpmnFile provideForTextPrompt(TextPrompt userDescription) {
        ChatModifiableObject<BpmnModel> chatModifiableBpmnModel = new ChatModifiableBpmnModel();
        OpenAIChatSession chatSession = chatSessionFactory.createNewSession(aiModel, temperature);
        PromptingState promptProvider = PromptingStrategyFactory.getPromptProvider(PromptingStrategy.PROMPT_ENRICHMENT, userDescription.content());

        ChatMessage responseMessage;
        boolean promptingFinished = false;
        while (!promptingFinished) {
            List<ChatMessage> prompt = promptProvider.getPromptForCurrentState();
            if (promptProvider.isFunctionCallingStep()) {
                responseMessage = chatSession.generateResponseFromPrompt(prompt, chatModifiableBpmnModel.getChatCallableInterface());
            } else {
                responseMessage = chatSession.generateResponseFromPrompt(prompt);
            }

            if (promptProvider.hasNextState()) {
                promptProvider = promptProvider.getNextState(responseMessage.getContent());
            } else {
                promptingFinished = true;
            }
        }

        Logging.logInfoMessage("Finished model generation");

        BpmnModel bpmnModel = chatModifiableBpmnModel.getObjectInstance();
        layouting.layoutModel(bpmnModel);

        return BpmnFile.fromModel(bpmnModel);
    }

}
