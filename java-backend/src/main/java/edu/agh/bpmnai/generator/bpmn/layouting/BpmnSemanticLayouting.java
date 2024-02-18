package edu.agh.bpmnai.generator.bpmn.layouting;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BpmnSemanticLayouting {

    public BpmnModel layoutModel(BpmnModel model) {
        BpmnModel layoutedModel = model.getCopy();
        double currentX = 0;
        double currentY = 0;
        String currentElementId = layoutedModel.findStartEvents().iterator().next();
        List<String> boundary = new ArrayList<>();
        boundary.add(currentElementId);
        while (!boundary.isEmpty()) {
            String nextElementId = boundary.get(0);
            boundary.remove(0);
            layoutedModel.setPositionOfElement(nextElementId, currentX, currentY);
            currentX += 100;
            boundary.addAll(layoutedModel.findSuccessors(nextElementId));
        }
        return layoutedModel;
    }
}
