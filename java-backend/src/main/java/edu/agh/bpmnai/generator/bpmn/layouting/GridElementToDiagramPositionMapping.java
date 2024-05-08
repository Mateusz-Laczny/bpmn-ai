package edu.agh.bpmnai.generator.bpmn.layouting;

import edu.agh.bpmnai.generator.bpmn.model.BpmnNodeType;
import org.springframework.stereotype.Service;

import static edu.agh.bpmnai.generator.bpmn.diagram.DiagramDimensions.*;

@Service
public class GridElementToDiagramPositionMapping {
    public Point2d apply(int cellWidth, int cellHeight, GridPosition cellPosition, BpmnNodeType elementType) {
        double xPos;
        double yPos;
        switch (elementType) {
            case START_EVENT, END_EVENT -> {
                xPos = cellWidth * cellPosition.x();
                yPos = cellHeight * cellPosition.y() + (0.5 * TASK_HEIGHT - (0.5 * EVENT_DIAMETER));
            }
            case TASK, OTHER_ELEMENT -> {
                xPos = cellWidth * cellPosition.x();
                yPos = cellHeight * cellPosition.y();
            }
            case XOR_GATEWAY, PARALLEL_GATEWAY -> {
                xPos = cellWidth * cellPosition.x();
                yPos = cellHeight * cellPosition.y() + (0.5 * TASK_HEIGHT - (0.5 * GATEWAY_DIAGONAL));
            }
            default -> throw new IllegalStateException("Unexpected element type: " + elementType);
        }

        return new Point2d(xPos, yPos);
    }
}
