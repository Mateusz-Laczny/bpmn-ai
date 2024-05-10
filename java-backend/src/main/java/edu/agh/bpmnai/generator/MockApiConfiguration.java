package edu.agh.bpmnai.generator;

import edu.agh.bpmnai.generator.openai.OpenAI;
import edu.agh.bpmnai.generator.v2.LlmService;
import edu.agh.bpmnai.generator.v2.MockLlmService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import static edu.agh.bpmnai.generator.openai.OpenAI.OpenAIModel.GPT_3_5_TURBO_16K;

@Configuration
@Profile("mock-api")
public class MockApiConfiguration {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public OpenAI.OpenAIModel openAIModel() {
        return GPT_3_5_TURBO_16K;
    }

    @Bean
    public LlmService llmService() {
        return new MockLlmService();
    }
}
