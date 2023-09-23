package edu.agh.bpmnai.generator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("paid-api")
public class MainConfiguration {
    @Bean
    public BpmnProvider bpmnProvider() {
        return new OpenAIBpmnProvider();
    }
}
