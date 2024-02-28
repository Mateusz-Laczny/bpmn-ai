package edu.agh.bpmnai.generator.bpmn.layouting;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
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
            SuccessorsNotInGrid nextElementId = boundary.remove(0);
            if (nextElementId.elements().size() == 1) {
                String singleSuccessorId = nextElementId.elements().get(0);

                int newCellX = nextElementId.predecessorPosition().x() + 1;
                int newCellY = nextElementId.predecessorPosition().y();
                grid.addCell(new Cell(newCellX, newCellY, singleSuccessorId));
                if (!alreadyVisitedElements.contains(singleSuccessorId)) {
                    List<String> elementSuccessors = layoutedModel.findSuccessors(singleSuccessorId).stream().filter(id -> !alreadyVisitedElements.contains(id)).toList();
                    boundary.add(new SuccessorsNotInGrid(new GridPosition(newCellX, newCellY), elementSuccessors));
                    alreadyVisitedElements.add(singleSuccessorId);
                }
            } else {
                int newCellX = nextElementId.predecessorPosition().x() + 1;
                int predecessorY = nextElementId.predecessorPosition().y();
                int currentYOffset = -1;
                boolean insertAbove = true;
                for (String successorId : nextElementId.elements()) {
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

    private record SuccessorsNotInGrid(GridPosition predecessorPosition, List<String> elements) {
    }
}
