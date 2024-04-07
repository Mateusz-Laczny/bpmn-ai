package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
import edu.agh.bpmnai.generator.bpmn.model.BpmnElementType;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class ModelPostProcessing {

    public void apply(BpmnManagedReference modelReference) {
        BpmnModel model = modelReference.getCurrentValue();
        for (String gatewayId : model.findElementsOfType(BpmnElementType.GATEWAY)) {
            Set<String> gatewaySuccessors = model.findSuccessors(gatewayId);
            if (gatewaySuccessors.size() == 1) {
                String gatewaySuccessorId = gatewaySuccessors.iterator().next();
                for (String predecessorId : model.findPredecessors(gatewayId)) {
                    model.addUnlabelledSequenceFlow(predecessorId, gatewaySuccessorId);
                }

                model.removeFlowNode(gatewayId);
            }
        }

        modelReference.setValue(model);
    }
}
