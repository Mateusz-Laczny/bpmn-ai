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
            String predecessorElementId,
            String elementStartId,
            @Nullable String elementEndId,
            BpmnModel model
    ) {
        if (!model.doesIdExist(predecessorElementId)) {
            return Result.error("Predecessor element '%s' does not exist".formatted(predecessorElementId));
        }
        Set<String> predecessorElementSuccessorsBeforeModification = model.findSuccessors(predecessorElementId);
        if (predecessorElementSuccessorsBeforeModification.size() > 1) {
            log.warn("Predecessor element '{}' has more than one successor", predecessorElementId);
        }

        @Nullable String predecessorElementSuccessor = null;
        if (!predecessorElementSuccessorsBeforeModification.isEmpty()) {
            predecessorElementSuccessor = predecessorElementSuccessorsBeforeModification.iterator().next();
            if (model.getNodeType(predecessorElementSuccessor).get() == END_EVENT) {
                model.removeElement(predecessorElementSuccessor);
            }
            model.clearSuccessors(predecessorElementId);
        }

        Result<String, AddSequenceFlowError> addStartSequenceFlowResult = model.addUnlabelledSequenceFlow(
                predecessorElementId,
                elementStartId
        );

        if (addStartSequenceFlowResult.isError()) {
            switch (addStartSequenceFlowResult.getError()) {
                case SOURCE_ELEMENT_DOES_NOT_EXIST -> throw new IllegalStateException(
                        "Predecessor element must exist at this point");
                case TARGET_ELEMENT_DOES_NOT_EXIST -> {
                    return Result.error("Element '%s' does not exist in the diagram".formatted(elementStartId));
                }
                case ELEMENTS_ALREADY_CONNECTED -> log.info(
                        "Attempted to add sequence flow between already connected elements '%s' and '%s'".formatted(
                                predecessorElementId,
                                elementStartId
                        ));
            }
        }

        if (predecessorElementSuccessor != null && elementEndId != null) {
            Result<String, AddSequenceFlowError> addEndSequenceFlowResult = model.addUnlabelledSequenceFlow(
                    elementEndId,
                    predecessorElementSuccessor
            );

            if (addEndSequenceFlowResult.isError()) {
                switch (addEndSequenceFlowResult.getError()) {
                    case SOURCE_ELEMENT_DOES_NOT_EXIST -> {
                        return Result.error("Element '%s' does not exist in the diagram".formatted(elementStartId));
                    }
                    case TARGET_ELEMENT_DOES_NOT_EXIST -> throw new IllegalStateException(
                            "Element with id '%s' no longer exists in the model".formatted(predecessorElementSuccessor));
                    case ELEMENTS_ALREADY_CONNECTED -> log.info(
                            "Attempted to add sequence flow between already connected elements '%s' and '%s'".formatted(
                                    elementEndId,
                                    predecessorElementSuccessor
                            ));
                }
            }
        }

        return Result.ok(null);
    }
}
