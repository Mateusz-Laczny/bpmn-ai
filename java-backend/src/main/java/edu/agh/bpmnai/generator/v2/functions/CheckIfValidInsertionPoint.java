package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.BpmnNodeType;
import edu.agh.bpmnai.generator.datatype.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnNodeType.PARALLEL_GATEWAY;
import static edu.agh.bpmnai.generator.bpmn.model.BpmnNodeType.XOR_GATEWAY;

@Service
@Slf4j
public class CheckIfValidInsertionPoint {

    public Result<Void, String> apply(BpmnModel model, String insertionPointId) {
        if (!model.nodeIdExist(insertionPointId)) {
            return Result.error("Insertion point '%s' does not exist, please select a valid insertion point.".formatted(
                    insertionPointId));
        }

        Set<String> insertionPointSuccessorsBeforeModification = model.findSuccessors(insertionPointId);
        BpmnNodeType insertionPointNodeType = model.getNodeType(insertionPointId).orElseThrow();
        boolean insertionPointPredecessorIsAGateway = insertionPointNodeType == XOR_GATEWAY
                                                      || insertionPointNodeType
                                                         == PARALLEL_GATEWAY;
        if (insertionPointSuccessorsBeforeModification.size() > 1 && !insertionPointPredecessorIsAGateway) {
            return Result.error(
                    ("Insertion point '%s' is not valid, since it has more than one successor and is not a gateway; "
                     + "chose a valid insertion"
                     + " point.").formatted(
                            insertionPointId));
        }

        return Result.ok(null);
    }
}
