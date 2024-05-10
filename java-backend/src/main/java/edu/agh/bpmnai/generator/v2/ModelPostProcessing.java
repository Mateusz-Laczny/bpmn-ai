package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnNodeType.*;

@Service
@Slf4j
public class ModelPostProcessing {

    public void apply(BpmnManagedReference modelReference) {
        BpmnModel model = modelReference.getCurrentValue();
        Set<String> allGateways = model.findElementsOfType(XOR_GATEWAY);
        allGateways.addAll(model.findElementsOfType(PARALLEL_GATEWAY));
        for (String gatewayId : allGateways) {
            Set<String> gatewaySuccessors = model.findSuccessors(gatewayId);
            Set<String> gatewayPredecessors = model.findPredecessors(gatewayId);
            if (gatewaySuccessors.size() == 1 && gatewayPredecessors.size() == 1) {
                log.debug(
                        "Gateway '{}' has only one successor and one predecessor and will be cut out from the model",
                        gatewayId
                );
                String gatewaySuccessorId = gatewaySuccessors.iterator().next();
                String gatewayPredecessor = gatewayPredecessors.iterator().next();
                model.addUnlabelledSequenceFlow(gatewayPredecessor, gatewaySuccessorId);
                model.removeFlowNode(gatewayId);
            }
        }

        for (String flowNode : model.getFlowNodes()) {
            boolean nonEndEventNodeWithoutSuccessors =
                    model.getNodeType(flowNode).orElseThrow() != END_EVENT && model.findSuccessors(flowNode).isEmpty();
            if (nonEndEventNodeWithoutSuccessors) {
                String endEventId = model.addEndEvent();
                model.addUnlabelledSequenceFlow(flowNode, endEventId);
            }
        }

        modelReference.setValue(model);
    }
}
