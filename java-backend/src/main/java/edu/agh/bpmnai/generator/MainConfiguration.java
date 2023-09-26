package edu.agh.bpmnai.generator;

import edu.agh.bpmnai.generator.bpmn.BpmnProvider;
import edu.agh.bpmnai.generator.bpmn.OpenAIBpmnProvider;
import edu.agh.bpmnai.generator.openai.OpenAIChatSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("paid-api")
public class MainConfiguration {
    @Bean
    public BpmnProvider bpmnProvider(@Autowired OpenAIChatSessionFactory chatSessionFactory) {
        return new OpenAIBpmnProvider(chatSessionFactory, layouting);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
