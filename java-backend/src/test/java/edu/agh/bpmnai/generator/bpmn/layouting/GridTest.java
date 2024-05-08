package edu.agh.bpmnai.generator.bpmn.layouting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GridTest {

    Grid grid;

    @BeforeEach
    void setUp() {
        grid = new Grid();
    }

    @Test
    void returns_correct_number_of_rows() {
        grid.addCell(new Cell(new GridPosition(0, 0), "a"));
        grid.addCell(new Cell(new GridPosition(0, 1), "a"));
        grid.addCell(new Cell(new GridPosition(1, 2), "a"));
        assertEquals(3, grid.getNumberOfRows());
    }
}