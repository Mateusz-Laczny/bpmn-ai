package edu.agh.bpmnai.generator.bpmn.layouting;

public record Cell(GridPosition gridPosition, String idOfElementInside) {

    public Cell(int x, int y, String idOfElementInside) {
        this(new GridPosition(x, y), idOfElementInside);
    }

    public int x() {
        return gridPosition.x();
    }

    public int y() {
        return gridPosition.y();
    }
}
