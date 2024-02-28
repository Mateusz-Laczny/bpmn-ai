package edu.agh.bpmnai.generator.bpmn.layouting;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.*;

public class Grid {
    private final Table<Integer, Integer, Cell> gridTable = HashBasedTable.create();

    private final Map<String, Cell> elementIdToCell = new HashMap<>();

    private final int rowSize = 0;

    private final int columnSize = 0;

    public Grid() {
    }

    public void addCell(Cell newCell) {
        gridTable.put(newCell.x(), newCell.y(), newCell);
        elementIdToCell.put(newCell.idOfElementInside(), newCell);
    }

    public Optional<Cell> findCellByIdOfElementInside(String idOfElementToFind) {
        return Optional.ofNullable(elementIdToCell.get(idOfElementToFind));
    }

    public Collection<Cell> allCells() {
        return gridTable.values();
    }

    public void shiftDown(int shiftAmount) {
        for (Cell cell : List.copyOf(gridTable.values())) {
            gridTable.remove(cell.x(), cell.y());
            var updatedCell = new Cell(cell.x(), cell.y() + shiftAmount, cell.idOfElementInside());
            gridTable.put(updatedCell.x(), updatedCell.y(), updatedCell);
        }
    }
}
