package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnElementType.GATEWAY;

@Service
@Slf4j
public class ModelPostProcessing {

    public void apply(BpmnManagedReference modelReference) {
        BpmnModel model = modelReference.getCurrentValue();
        for (String gatewayId : model.findElementsOfType(GATEWAY)) {
            Set<String> gatewaySuccessors = model.findSuccessors(gatewayId);
            if (gatewaySuccessors.size() == 1) {
                log.debug("Gateway '{}' has only one successor and will be cut out from the model", gatewayId);
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
