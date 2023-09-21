package edu.agh.bpmnai.generator.openai;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import java.time.Duration;

public class OpenAIApiThrottling {

    public static final Bucket bucket;

    static {
        Bandwidth limit = Bandwidth.simple(OpenAI.openAIApiTokenPerMinuteRateLimit, Duration.ofMinutes(1));
        bucket = Bucket.builder().addLimit(limit).build();
    }
}
