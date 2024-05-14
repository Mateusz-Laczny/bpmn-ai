package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.bpmn.model.AddSequenceFlowError;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnNodeType.END_EVENT;

@Service
@Slf4j
public class InsertElementIntoDiagram {
    public Result<Void, String> apply(
            String insertionPointId,
            String subprocessStartId,
            @Nullable String subprocessEndId,
            BpmnModel model
    ) {
        if (!model.nodeIdExist(insertionPointId)) {
            return Result.error("Insertion point '%s' does not exist".formatted(insertionPointId));
        }
        Set<String> insertionPointSuccessorsBeforeModification = model.findSuccessors(insertionPointId);
        if (insertionPointSuccessorsBeforeModification.size() > 1) {
            return Result.error(
                    ("Insertion point '%s' is not valid, since it has more than one successor; chose a valid insertion"
                     + " point.").formatted(
                            insertionPointId));
        }

        @Nullable String insertionPointSuccessor = null;
        if (!insertionPointSuccessorsBeforeModification.isEmpty()) {
            insertionPointSuccessor = insertionPointSuccessorsBeforeModification.iterator().next();
            if (model.getNodeType(insertionPointSuccessor).get() == END_EVENT) {
                model.removeElement(insertionPointSuccessor);
            }
            model.clearSuccessors(insertionPointId);
        }

        Result<String, AddSequenceFlowError> addStartSequenceFlowResult = model.addUnlabelledSequenceFlow(
                insertionPointId,
                subprocessStartId
        );

        if (addStartSequenceFlowResult.isError()) {
            switch (addStartSequenceFlowResult.getError()) {
                case SOURCE_ELEMENT_DOES_NOT_EXIST -> throw new IllegalStateException(
                        "Predecessor element must exist at this point");
                case TARGET_ELEMENT_DOES_NOT_EXIST -> {
                    return Result.error("Element '%s' does not exist in the diagram".formatted(subprocessStartId));
                }
                case ELEMENTS_ALREADY_CONNECTED -> log.info(
                        "Attempted to add sequence flow between already connected elements '%s' and '%s'".formatted(
                                insertionPointId,
                                subprocessStartId
                        ));
            }
        }

        if (insertionPointSuccessor != null && subprocessEndId != null) {
            Result<String, AddSequenceFlowError> addEndSequenceFlowResult = model.addUnlabelledSequenceFlow(
                    subprocessEndId,
                    insertionPointSuccessor
            );

            if (addEndSequenceFlowResult.isError()) {
                switch (addEndSequenceFlowResult.getError()) {
                    case SOURCE_ELEMENT_DOES_NOT_EXIST -> {
                        return Result.error("Element '%s' does not exist in the diagram".formatted(subprocessStartId));
                    }
                    case TARGET_ELEMENT_DOES_NOT_EXIST -> throw new IllegalStateException(
                            "Element with id '%s' no longer exists in the model".formatted(insertionPointSuccessor));
                    case ELEMENTS_ALREADY_CONNECTED -> log.info(
                            "Attempted to add sequence flow between already connected elements '%s' and '%s'".formatted(
                                    subprocessEndId,
                                    insertionPointSuccessor
                            ));
                }
            }
        }

        return Result.ok(null);
    }
}
