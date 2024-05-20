package edu.agh.bpmnai.generator.bpmn.layouting;

public record GridPosition(int x, int y) {
    public GridPosition withY(int newY) {
        return new GridPosition(x, newY);
    }

    public GridPosition withYDifference(int yDifference) {
        return new GridPosition(x, y + yDifference);
    }

    public GridPosition withX(int newX) {
        return new GridPosition(newX, y);
    }

    public GridPosition copy() {
        return new GridPosition(x, y);
    }
}
