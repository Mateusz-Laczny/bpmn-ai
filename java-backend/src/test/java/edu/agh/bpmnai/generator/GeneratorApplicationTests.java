package edu.agh.bpmnai.generator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = GeneratorApplication.class)
@ActiveProfiles("paid-api")
class GeneratorApplicationTests {

    @Test
    void contextLoads() {
    }

}
