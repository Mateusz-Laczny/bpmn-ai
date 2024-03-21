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
        String currentElementId = layoutedModel.getStartEvent();
        grid.addCell(new Cell(0, 0, currentElementId));
        List<SuccessorsNotInGrid> boundary = new ArrayList<>();
        boundary.add(new SuccessorsNotInGrid(new GridPosition(0, 0), List.copyOf(layoutedModel.findSuccessors(currentElementId))));
        var alreadyVisitedElements = new HashSet<>();
        alreadyVisitedElements.add(currentElementId);
        while (!boundary.isEmpty()) {
            SuccessorsNotInGrid processedElements = boundary.remove(0);
            if (processedElements.elements().size() == 1) {
                String singleSuccessorId = processedElements.elements().get(0);

                int newCellX = processedElements.predecessorPosition().x() + 1;
                int newCellY = processedElements.predecessorPosition().y();
                Collection<String> elementPredecessors = model.findPredecessors(singleSuccessorId);
                if (elementPredecessors.size() > 1) {
                    Result<Integer, String> findRowResult = findRowForElement(elementPredecessors, grid);
                    if (findRowResult.isError()) {
                        log.warn("Could not calculate row for element, predecessor with id '{}' is not in grid", findRowResult.getError());
                    } else {
                        newCellY = findRowForElement(elementPredecessors, grid).getValue();
                    }
                }
                grid.addCell(new Cell(newCellX, newCellY, singleSuccessorId));
                if (!alreadyVisitedElements.contains(singleSuccessorId)) {
                    List<String> elementSuccessors = layoutedModel.findSuccessors(singleSuccessorId).stream().filter(id -> !alreadyVisitedElements.contains(id)).toList();
                    boundary.add(new SuccessorsNotInGrid(new GridPosition(newCellX, newCellY), elementSuccessors));
                    alreadyVisitedElements.add(singleSuccessorId);
                }
            } else {
                int newCellX = processedElements.predecessorPosition().x() + 1;
                int predecessorY = processedElements.predecessorPosition().y();
                int currentYOffset = -1;
                boolean insertAbove = true;
                for (String successorId : processedElements.elements()) {
                    if (alreadyVisitedElements.contains(successorId)) {
                        continue;
                    }

                    if (predecessorY + currentYOffset < 0) {
                        grid.shiftDown(-currentYOffset);
                        predecessorY += -currentYOffset;
                    }

                    Cell newCell = new Cell(newCellX, predecessorY + currentYOffset, successorId);
                    grid.addCell(newCell);
                    if (!alreadyVisitedElements.contains(successorId)) {
                        List<String> elementSuccessors = layoutedModel.findSuccessors(successorId).stream().filter(id -> !alreadyVisitedElements.contains(id)).toList();
                        boundary.add(new SuccessorsNotInGrid(new GridPosition(newCell.x(), newCell.y()), elementSuccessors));
                        alreadyVisitedElements.add(successorId);
                    }

                    if (insertAbove) {
                        currentYOffset = -currentYOffset;
                        insertAbove = false;
                    } else {
                        currentYOffset = -(currentYOffset + 1);
                        insertAbove = true;
                    }
                }
            }
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

    private record SuccessorsNotInGrid(GridPosition predecessorPosition, List<String> elements) {
    }
}
