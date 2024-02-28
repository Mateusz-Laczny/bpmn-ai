package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.v2.Description;
import jakarta.annotation.Nullable;

import java.util.List;

public record UserDescriptionReasoningDto(
        @Description("All information that can help modeling the process, which is known or can be reasonably assumed") String background,
        @Description("Thoughts about the description, what process does it describe, what is missing and how it could be improved.") List<Thought> thoughts,
        @Description("Optional message to the user, asking about the missing details. Should be as specific as possible. This is the only parameter that will be visible to the user") @Nullable String messageToTheUser) {
}

record Thought(
        @Description("Text of the thought.") String thoughtText,
        @Description("How useful is the thought, on a scale from 1 to 10, where 10 indicates best.") String usefulnessScore
) {
}
