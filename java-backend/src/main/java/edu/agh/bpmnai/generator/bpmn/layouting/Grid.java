package edu.agh.bpmnai.generator.bpmn.layouting;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.agh.bpmnai.generator.datatype.Result;

import java.util.*;

import static edu.agh.bpmnai.generator.bpmn.layouting.MoveError.ORIGINAL_CELL_EMPTY;
import static edu.agh.bpmnai.generator.bpmn.layouting.MoveError.TARGET_CELL_OCCUPIED;

public class Grid {
    private final Table<Integer, Integer, Cell> gridTable = HashBasedTable.create();

    private final Map<String, Cell> elementIdToCell = new HashMap<>();

    private final int rowSize = 0;

    private final int columnSize = 0;

    public Grid() {
    }

    public void addCell(Cell newCell) {
        if (gridTable.contains(newCell.x(), newCell.y())) {
            throw new IllegalArgumentException("Cell %s:%s is already occupied by %s".formatted(
                    newCell.x(),
                    newCell.y(),
                    gridTable.get(
                            newCell.x(),
                            newCell.y()
                    ).idOfElementInside()
            ));
        }
        gridTable.put(newCell.x(), newCell.y(), newCell);
        elementIdToCell.put(newCell.idOfElementInside(), newCell);
    }

    public Optional<Cell> findCellByIdOfElementInside(String idOfElementToFind) {
        return Optional.ofNullable(elementIdToCell.get(idOfElementToFind));
    }

    public Collection<Cell> allCells() {
        return gridTable.values();
    }

    public void shiftRows(int shiftAmount) {
        for (Cell cell : List.copyOf(gridTable.values())) {
            gridTable.remove(cell.x(), cell.y());
            var updatedCell = new Cell(cell.x(), cell.y() + shiftAmount, cell.idOfElementInside());
            gridTable.put(updatedCell.x(), updatedCell.y(), updatedCell);
        }
    }

    public void shiftColumnInYAxis(int columnIndex, int shiftAmount) {
        for (Cell cell : List.copyOf(gridTable.row(columnIndex).values())) {
            moveCell(cell.gridPosition(), cell.gridPosition().withY(cell.gridPosition().y() + shiftAmount));
        }
    }

    public boolean isCellOccupied(int x, int y) {
        return gridTable.contains(x, y);
    }

    public boolean isCellOccupied(GridPosition position) {
        return gridTable.contains(position.x(), position.y());
    }

    public Result<Void, MoveError> moveCell(GridPosition originalPosition, GridPosition targetPosition) {
        if (!isCellOccupied(originalPosition)) {
            return Result.error(ORIGINAL_CELL_EMPTY);
        }

        if (isCellOccupied(targetPosition)) {
            return Result.error(TARGET_CELL_OCCUPIED);
        }

        Cell cellToMove = gridTable.get(originalPosition.x(), originalPosition.y());
        gridTable.remove(originalPosition.x(), originalPosition.y());
        Cell movedCell = new Cell(targetPosition, cellToMove.idOfElementInside());
        addCell(movedCell);
        return Result.ok(null);
    }

    public int getNumberOfRows() {
        return gridTable.columnKeySet().stream().max(Integer::compareTo).orElse(0);
    }

    public void shiftElementinYAxis(String key, int i) {

    }
}
