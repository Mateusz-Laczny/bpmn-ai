package edu.agh.bpmnai.generator.bpmn.layouting;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class BpmnSemanticLayouting {

    private final int cellWidth;

    private final int cellHeight;

    public BpmnSemanticLayouting(@Value("100") int cellWidth, @Value("60") int cellHeight) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
    }

    private static void moveOrAddCellToGrid(Grid grid, String cellContent, int updatedCellX, int updatedCellY) {
        Optional<Cell> currentCellOfElement = grid.findCellByIdOfElementInside(cellContent);
        if (currentCellOfElement.isPresent()) {
            grid.moveCell(currentCellOfElement.get().gridPosition(), new GridPosition(updatedCellX, updatedCellY));
        } else {
            grid.addCell(new Cell(updatedCellX, updatedCellY, cellContent));
        }
    }

    public BpmnModel layoutModel(BpmnModel model) {
        BpmnModel layoutedModel = model.getCopy();
        var grid = new Grid();
        grid.addCell(new Cell(0, 0, layoutedModel.getStartEvent()));
        List<String> boundary = new ArrayList<>(layoutedModel.findSuccessors(layoutedModel.getStartEvent()));
        while (!boundary.isEmpty()) {
            String processedElementId = boundary.remove(0);
            LinkedHashSet<String> elementSuccessors = model.findSuccessors(processedElementId);
            Optional<Cell> elementGridCell = grid.findCellByIdOfElementInside(processedElementId);
            if (elementGridCell.isPresent() && elementSuccessors.stream().allMatch(successiorId -> grid.findCellByIdOfElementInside(successiorId).map(successorCell -> successorCell.x() > elementGridCell.get().x()).orElse(false))) {
                continue;
            }

            LinkedHashSet<String> elementPredecessors = model.findPredecessors(processedElementId);
            int numberOfPredecessorsInGrid = (int) elementPredecessors.stream().filter(predecessorId -> grid.findCellByIdOfElementInside(predecessorId).isPresent()).count();

            if (numberOfPredecessorsInGrid == 1) {
                String singlePredecessorId = elementPredecessors.iterator().next();
                Optional<Cell> predecessorCellOptional = grid.findCellByIdOfElementInside(singlePredecessorId);
                boolean isPartOfACycle = predecessorCellOptional.isEmpty();
                if (!isPartOfACycle) {
                    Cell predecessorCell = predecessorCellOptional.get();

                    int updatedCellX = predecessorCell.x() + 1;
                    int updatedCellY = predecessorCell.y();

                    int overallShift = 0;
                    while (grid.isCellOccupied(updatedCellX, updatedCellY)) {
                        grid.shiftColumnInYAxis(updatedCellX, 2);
                        overallShift += 2;
                        GridPosition updatedPredecessorPosition = predecessorCell.gridPosition().withY((predecessorCell.gridPosition().y() + overallShift) / 2);
                        grid.moveCell(predecessorCell.gridPosition(), updatedPredecessorPosition);
                    }

                    moveOrAddCellToGrid(grid, processedElementId, updatedCellX, updatedCellY);
                }
            } else if (numberOfPredecessorsInGrid > 1) {
                int maxPredecessorX = elementPredecessors.stream()
                        .mapToInt(elementId -> grid.findCellByIdOfElementInside(elementId).map(Cell::x).orElse(-1))
                        .max()
                        .getAsInt();
                int updatedCellY = 0;
                Result<Integer, String> findRowResult = findRowForElementWithMultiplePredecessors(elementPredecessors, grid);
                if (findRowResult.isError()) {
                    log.warn("Could not calculate row for element, predecessor with id '{}' is not in grid", findRowResult.getError());
                } else {
                    updatedCellY = findRowResult.getValue();
                }

                moveOrAddCellToGrid(grid, processedElementId, maxPredecessorX + 1, updatedCellY);
            }

            boundary.addAll(elementSuccessors);
        }

        for (Cell cell : grid.allCells()) {
            layoutedModel.setPositionOfElement(cell.idOfElementInside(), cellWidth * cell.x(), cellHeight * cell.y());
        }

        return layoutedModel;
    }

    private Result<Integer, String> findRowForElementWithMultiplePredecessors(Collection<String> elementPredecessors, Grid grid) {
        int maxPredecessorColumnIndex = -1;
        for (String elementPredecessor : elementPredecessors) {
            Optional<Cell> elementCell = grid.findCellByIdOfElementInside(elementPredecessor);
            if (elementCell.isEmpty()) {
                return Result.error(elementPredecessor);
            }

            if (elementCell.get().y() > maxPredecessorColumnIndex) {
                maxPredecessorColumnIndex = elementCell.get().y();
            }
        }

        return Result.ok(maxPredecessorColumnIndex - (elementPredecessors.size() / 2));
    }
}
