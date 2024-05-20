package edu.agh.bpmnai.generator;

import edu.agh.bpmnai.generator.openai.OpenAI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import static edu.agh.bpmnai.generator.openai.OpenAI.OpenAIModel.GPT_4_O;

@Configuration
@Profile("paid-api")
public class MainConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public OpenAI.OpenAIModel openAIModel() {
        return GPT_4_O;
    }
}
