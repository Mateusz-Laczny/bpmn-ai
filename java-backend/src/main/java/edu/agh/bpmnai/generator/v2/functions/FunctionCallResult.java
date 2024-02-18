package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.ChatMessageDto;

import java.util.Optional;

public record FunctionCallResult(Optional<ChatMessageDto> response, boolean needsResponseFromUser) {
    public static FunctionCallResult withResponse(ChatMessageDto response) {
        return new FunctionCallResult(Optional.of(response), false);
    }
}
