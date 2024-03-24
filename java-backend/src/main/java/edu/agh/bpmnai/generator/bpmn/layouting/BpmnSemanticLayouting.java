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

                    int newCellX = predecessorCell.x() + 1;
                    int newCellY = predecessorCell.y();

                    int overallShift = 0;
                    while (grid.isCellOccupied(newCellX, newCellY)) {
                        grid.shiftColumnInYAxis(newCellX, 2);
                        overallShift += 2;
                        GridPosition updatedPredecessorPosition = predecessorCell.gridPosition().withY((predecessorCell.gridPosition().y() + overallShift) / 2);
                        grid.moveCell(predecessorCell.gridPosition(), updatedPredecessorPosition);
                    }

                    grid.addCell(new Cell(newCellX, newCellY, processedElementId));
                }
            } else if (numberOfPredecessorsInGrid > 1) {
                int maxPredecessorX = elementPredecessors.stream()
                        .mapToInt(elementId -> grid.findCellByIdOfElementInside(elementId).map(Cell::x).orElse(-1))
                        .max()
                        .getAsInt();
                int newCellY = 0;
                Result<Integer, String> findRowResult = findRowForElement(elementPredecessors, grid);
                if (findRowResult.isError()) {
                    log.warn("Could not calculate row for element, predecessor with id '{}' is not in grid", findRowResult.getError());
                } else {
                    newCellY = findRowResult.getValue();
                }

                grid.addCell(new Cell(maxPredecessorX + 1, newCellY, processedElementId));
            }

            boundary.addAll(elementSuccessors);
        }

        for (Cell cell : grid.allCells()) {
            layoutedModel.setPositionOfElement(cell.idOfElementInside(), cellWidth * cell.x(), cellHeight * cell.y());
        }

        return layoutedModel;
    }

    private Result<Integer, String> findRowForElement(Collection<String> elementPredecessors, Grid grid) {
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
