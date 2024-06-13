package edu.agh.bpmnai.generator.v2;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.BpmnNodeType;
import edu.agh.bpmnai.generator.v2.session.ImmutableSessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class NodeIdToModelInterfaceIdFunction {

    public Map<String, String> apply(Set<String> nodeIds, ImmutableSessionState sessionState) {
        BpmnModel model = sessionState.bpmnModel();
        BiMap<String, String> nodeIdToModelInterfacingId = HashBiMap.create(sessionState.nodeIdToModelInterfaceId());
        for (String nodeId : nodeIds) {
            int duplicateCounter = 1;
            BpmnNodeType nodeType = model.getNodeType(nodeId).orElseThrow();
            String finalModelInterfacingId = nodeType.asString();
            while (nodeIdToModelInterfacingId.inverse().containsKey(finalModelInterfacingId)) {
                finalModelInterfacingId = nodeType.asString() + '-' + duplicateCounter;
                duplicateCounter += 1;
            }

            nodeIdToModelInterfacingId.put(nodeId, finalModelInterfacingId);

            log.info(
                    "Final model interface id for element '{}': '{}'",
                    model.getHumanReadableId(nodeId).orElseThrow().asString(),
                    finalModelInterfacingId
            );
        }

        return nodeIdToModelInterfacingId;
    }
}
