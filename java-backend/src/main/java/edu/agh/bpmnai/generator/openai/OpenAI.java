package edu.agh.bpmnai.generator.openai;

import com.knuddels.jtokkit.api.EncodingType;
import edu.agh.bpmnai.generator.Encodings;

public class OpenAI {

    public static final String openAIApiUrl = "https://api.openai.com/v1/chat/completions";
    public static final String openAIApiKey = System.getenv("OPENAI_API_KEY");

    public static final int openAIApiTokenPerMinuteRateLimit = 180_000;

    public static int getNumberOfTokens(String text, OpenAIModel model) {
        return Encodings.getNumberOfTokensAfterEncoding(text, model.getModelProperties().encodingType());
    }

    public enum OpenAIModel {
        GPT_3_5_TURBO_16K(new OpenAIModelProperties("gpt-3.5-turbo-16k-0613", 16_385, 3, 1, EncodingType.CL100K_BASE)),
        GPT_3_5_TURBO(new OpenAIModelProperties("gpt-3.5-turbo", 16_385, 3, 1, EncodingType.CL100K_BASE)),
        GPT_4_TURBO(new OpenAIModelProperties("gpt-4-turbo", 128_000, 3, 1, EncodingType.CL100K_BASE)),
        GPT_4_O(new OpenAIModelProperties("gpt-4o", 128_000, 3, 1, EncodingType.CL100K_BASE));

        private final OpenAIModelProperties modelProperties;

        OpenAIModel(OpenAIModelProperties modelProperties) {
            this.modelProperties = modelProperties;
        }

        public OpenAIModelProperties getModelProperties() {
            return modelProperties;
        }
    }

    public record OpenAIModelProperties(String name, int maxNumberOfTokens, int tokensPerMessage, int tokensPerName,
                                        EncodingType encodingType) {
    }
}
