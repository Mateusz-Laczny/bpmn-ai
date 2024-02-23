package edu.agh.bpmnai.generator.v2;

public record ToolToCallDto(String type, FunctionToCallDto function) {

    public ToolToCallDto(String toolNameToCall) {
        this("function", new FunctionToCallDto(toolNameToCall));
    }

    private record FunctionToCallDto(String name) {

    }
}
