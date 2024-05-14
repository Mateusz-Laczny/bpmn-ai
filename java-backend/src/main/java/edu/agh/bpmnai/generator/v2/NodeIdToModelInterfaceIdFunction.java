package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.BpmnNodeType;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@Slf4j
public class NodeIdToModelInterfaceIdFunction implements Function<String, String> {

    private final SessionStateStore sessionStateStore;

    @Autowired
    public NodeIdToModelInterfaceIdFunction(SessionStateStore sessionStateStore) {
        this.sessionStateStore = sessionStateStore;
    }

    @Override
    public String apply(String nodeId) {
        BpmnModel model = sessionStateStore.model();
        int duplicateCounter = 1;
        BpmnNodeType nodeType = model.getNodeType(nodeId).orElseThrow();
        String finalId = nodeType.asString();
        while (sessionStateStore.getNodeId(finalId).isPresent()) {
            finalId = nodeType.asString() + '-' + duplicateCounter;
            duplicateCounter += 1;
        }

        log.info(
                "Final model interface id for element '{}': '{}'",
                model.getHumanReadableId(nodeId).orElseThrow().asString(),
                finalId
        );

        return finalId;
    }
}
