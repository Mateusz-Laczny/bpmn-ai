package edu.agh.bpmnai.generator.bpmn.layouting;

public record GridPosition(int x, int y) {
    public GridPosition withY(int newY) {
        return new GridPosition(x, newY);
    }
}
