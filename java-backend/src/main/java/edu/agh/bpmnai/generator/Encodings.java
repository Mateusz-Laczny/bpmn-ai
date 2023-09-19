package edu.agh.bpmnai.generator;

import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;

public class Encodings {
    private static final EncodingRegistry registry = com.knuddels.jtokkit.Encodings.newDefaultEncodingRegistry();

    public static int getNumberOfTokensAfterEncoding(String text, EncodingType encodingType) {
        Encoding encoding = registry.getEncoding(encodingType);
        return encoding.countTokens(text);
    }
}
