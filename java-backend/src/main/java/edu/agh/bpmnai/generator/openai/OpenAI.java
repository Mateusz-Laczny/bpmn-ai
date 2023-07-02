package edu.agh.bpmnai.generator.openai;

public class OpenAI {

    public static final String openAIApiUrl = "https://api.openai.com/v1/chat/completions";
    public static final String openAIApiKey = System.getenv("OPENAI_API_KEY");

    public static final int approximateTokensPerParagraph = 100;

    public static int getApproximateNumberOfTokens(String text) {
        return text.length() / 4;
    }

    public enum OpenAIModel {
        GPT_3_5_TURBO_16K(new OpenAIModelProperties("gpt-3.5-turbo-16k", 16_384));

        private final OpenAIModelProperties modelProperties;

        OpenAIModel(OpenAIModelProperties modelProperties) {
            this.modelProperties = modelProperties;
        }

        public OpenAIModelProperties getModelProperties() {
            return modelProperties;
        }
    }

    public record OpenAIModelProperties(String name, int maxNumberOfTokens) {
    }
}
