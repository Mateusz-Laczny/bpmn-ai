package edu.agh.bpmnai.generator.v2;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.session.ImmutableSessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnNodeType.*;

@Service
@Slf4j
public class ModelPostProcessing {

    private final NodeIdToModelInterfaceIdFunction nodeIdToModelInterfaceIdFunction;

    @Autowired
    public ModelPostProcessing(
            NodeIdToModelInterfaceIdFunction nodeIdToModelInterfaceIdFunction
    ) {
        this.nodeIdToModelInterfaceIdFunction = nodeIdToModelInterfaceIdFunction;
    }

    public ImmutableSessionState apply(ImmutableSessionState sessionState) {
        BpmnModel model = sessionState.bpmnModel();
        Set<String> allGateways = model.findElementsOfType(XOR_GATEWAY);
        allGateways.addAll(model.findElementsOfType(PARALLEL_GATEWAY));
        Set<String> removedElements = new HashSet<>();
        for (String gatewayId : allGateways) {
            Set<String> gatewaySuccessors = model.findSuccessors(gatewayId);
            Set<String> gatewayPredecessors = model.findPredecessors(gatewayId);
            if (gatewaySuccessors.size() == 1 && gatewayPredecessors.size() == 1) {
                log.debug(
                        "Gateway '{}' has only one successor and one predecessor and will be cut out from the model",
                        model.getHumanReadableId(gatewayId).orElseThrow()
                );
                String gatewaySuccessorId = gatewaySuccessors.iterator().next();
                String gatewayPredecessor = gatewayPredecessors.iterator().next();
                model.addUnlabelledSequenceFlow(gatewayPredecessor, gatewaySuccessorId);
                model.removeFlowNode(gatewayId);
                removedElements.add(gatewayId);
            }
        }

        Set<String> addedEndEventIds = new HashSet<>();
        for (String flowNode : model.getFlowNodes()) {
            boolean nonEndEventNodeWithoutSuccessors =
                    model.getNodeType(flowNode).orElseThrow() != END_EVENT && model.findSuccessors(flowNode).isEmpty();
            if (nonEndEventNodeWithoutSuccessors) {
                String endEventId = model.addEndEvent();
                model.addUnlabelledSequenceFlow(flowNode, endEventId);
                addedEndEventIds.add(endEventId);
            }
        }

        BiMap<String, String> updatedNodeIdToModelInterfaceIdMapping = HashBiMap.create();

        for (Entry<String, String> entry : sessionState.nodeIdToModelInterfaceId().entrySet()) {
            if (!removedElements.contains(entry.getKey())) {
                updatedNodeIdToModelInterfaceIdMapping.put(entry.getKey(), entry.getValue());
            }
        }

        sessionState =
                ImmutableSessionState.builder().from(sessionState).bpmnModel(model).nodeIdToModelInterfaceId(
                        nodeIdToModelInterfaceIdFunction.apply(addedEndEventIds, sessionState)).build();

        return ImmutableSessionState.builder()
                .from(sessionState)
                .bpmnModel(model)
                .nodeIdToModelInterfaceId(updatedNodeIdToModelInterfaceIdMapping)
                .build();
    }
}
