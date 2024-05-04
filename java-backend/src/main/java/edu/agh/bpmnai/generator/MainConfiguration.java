package edu.agh.bpmnai.generator;

import edu.agh.bpmnai.generator.bpmn.BpmnProvider;
import edu.agh.bpmnai.generator.bpmn.ChatDirectModificationBpmnProvider;
import edu.agh.bpmnai.generator.bpmn.layouting.GridBasedBpmnLayouting;
import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.openai.OpenAIChatSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import static edu.agh.bpmnai.generator.openai.OpenAI.OpenAIModel.GPT_3_5_TURBO_16K;

@Configuration
@Profile("paid-api")
public class MainConfiguration {

    @Bean
    public BpmnProvider bpmnProvider(
            @Autowired OpenAIChatSessionFactory chatSessionFactory,
            @Autowired GridBasedBpmnLayouting gridBasedBpmnLayouting
    ) {
        return new ChatDirectModificationBpmnProvider(chatSessionFactory, gridBasedBpmnLayouting);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public OpenAI.OpenAIModel openAIModel() {
        return GPT_3_5_TURBO_16K;
    }
}
