package edu.agh.bpmnai.generator;

import edu.agh.bpmnai.generator.bpmn.BpmnProvider;
import edu.agh.bpmnai.generator.bpmn.MockBpmnProvider;
import edu.agh.bpmnai.generator.bpmn.layouting.BpmnSemanticLayouting;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("mock-api")
public class MockApiConfiguration {
    @Bean
    public BpmnProvider bpmnProvider() {
        return new MockBpmnProvider();
    }

    @Bean
    BpmnSemanticLayouting bpmnSemanticLayouting() {
        return new BpmnSemanticLayouting(100, 60);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
