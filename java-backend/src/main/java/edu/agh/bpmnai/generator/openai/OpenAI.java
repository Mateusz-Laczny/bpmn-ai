package edu.agh.bpmnai.generator.openai;

import com.knuddels.jtokkit.api.EncodingType;
import edu.agh.bpmnai.generator.Encodings;

public class OpenAI {

    public static final String openAIApiUrl = "https://api.openai.com/v1/chat/completions";
    public static final String openAIApiKey = System.getenv("OPENAI_API_KEY");

    public static final int approximateTokensPerParagraph = 100;

    public static int getNumberOfTokens(String text, OpenAIModel model) {
        return Encodings.getNumberOfTokensAfterEncoding(text, model.getModelProperties().encodingType());
    }

    public enum OpenAIModel {
        GPT_3_5_TURBO_16K(new OpenAIModelProperties("gpt-3.5-turbo-16k-0613", 16_384, EncodingType.CL100K_BASE));

        private final OpenAIModelProperties modelProperties;

        OpenAIModel(OpenAIModelProperties modelProperties) {
            this.modelProperties = modelProperties;
        }

        public OpenAIModelProperties getModelProperties() {
            return modelProperties;
        }
    }

    public record OpenAIModelProperties(String name, int maxNumberOfTokens, EncodingType encodingType) {
    }
}
