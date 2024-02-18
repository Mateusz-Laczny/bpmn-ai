package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.v2.functions.ChatFunctionDto;

public record ChatToolDto(String type, ChatFunctionDto function) {
    public ChatToolDto(ChatFunctionDto function) {
        this("function", function);
    }
}
