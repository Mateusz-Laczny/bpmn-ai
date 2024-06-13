package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.bpmn.model.AddSequenceFlowError;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnNodeType.END_EVENT;

@Service
@Slf4j
public class InsertElementIntoDiagram {

    private final CheckIfValidInsertionPoint checkIfValidInsertionPoint;

    @Autowired
    public InsertElementIntoDiagram(CheckIfValidInsertionPoint checkIfValidInsertionPoint) {
        this.checkIfValidInsertionPoint = checkIfValidInsertionPoint;
    }

    public Result<Void, String> apply(
            String insertionPointId,
            String subprocessStartId,
            @Nullable String subprocessEndId,
            BpmnModel model
    ) {
        Result<Void, String> checkResult = checkIfValidInsertionPoint.apply(model, insertionPointId);
        if (checkResult.isError()) {
            return Result.error(checkResult.getError());
        }

        Set<String> insertionPointSuccessors = model.findSuccessors(insertionPointId);
        @Nullable String insertionPointSuccessor = null;
        if (!insertionPointSuccessors.isEmpty()) {
            insertionPointSuccessor = insertionPointSuccessors.iterator().next();
            if (model.getNodeType(insertionPointSuccessor).orElseThrow() == END_EVENT) {
                model.removeElement(insertionPointSuccessor);
                insertionPointSuccessor = null;
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
