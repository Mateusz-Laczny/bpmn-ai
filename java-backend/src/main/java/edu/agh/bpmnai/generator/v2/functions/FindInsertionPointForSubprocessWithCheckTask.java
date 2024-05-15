package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Optional;

import static edu.agh.bpmnai.generator.bpmn.model.HumanReadableId.isHumanReadableIdentifier;

@Service
@Slf4j
public class FindInsertionPointForSubprocessWithCheckTask {

    private final SessionStateStore sessionStateStore;

    private final CheckIfValidInsertionPoint checkIfValidInsertionPoint;

    @Autowired
    public FindInsertionPointForSubprocessWithCheckTask(
            SessionStateStore sessionStateStore,
            CheckIfValidInsertionPoint checkIfValidInsertionPoint
    ) {
        this.sessionStateStore = sessionStateStore;
        this.checkIfValidInsertionPoint = checkIfValidInsertionPoint;
    }

    public Result<InsertionPointFindResult, String> apply(
            String checkTaskString,
            @Nullable String insertionPointModelInterfaceIdString
    ) {
        BpmnModel model = sessionStateStore.model();
        String checkTaskId;
        if (isHumanReadableIdentifier(checkTaskString)) {
            String checkTaskModelInterfaceId = HumanReadableId.fromString(checkTaskString).id();
            Optional<String> checkTaskIdOptional = sessionStateStore.getNodeId(checkTaskModelInterfaceId);
            if (checkTaskIdOptional.isEmpty()) {
                return Result.error("Check task '%s' does not exist in the diagram".formatted(
                        checkTaskString));
            }

            return Result.ok(new InsertionPointFindResult(checkTaskIdOptional.get(), false));
        }
        if (insertionPointModelInterfaceIdString == null) {
            log.warn(
                    "Call unsuccessful, insertion point is null when check task '{}' does not exist in the "
                    + "diagram",
                    checkTaskString
            );
            return Result.error("Insertion point is null, when check task does not exist in the diagram.");
        }

        if (!isHumanReadableIdentifier(insertionPointModelInterfaceIdString)) {
            return Result.error("'%s' is not in the correct format".formatted(insertionPointModelInterfaceIdString));
        }

        HumanReadableId insertionPointModelFacingId = HumanReadableId.fromString(
                insertionPointModelInterfaceIdString);
        Optional<String> insertionPointModelId = sessionStateStore.getNodeId(insertionPointModelFacingId.id());
        if (insertionPointModelId.isEmpty()) {
            log.warn(
                    "Call unsuccessful, insertion point '{}' does not exist in the diagram",
                    insertionPointModelFacingId
            );
            return Result.error("Insertion point '%s' does not exist in the diagram".formatted(
                    insertionPointModelInterfaceIdString));
        }

        Result<Void, String> checkResult = checkIfValidInsertionPoint.apply(insertionPointModelId.get());
        if (checkResult.isError()) {
            return Result.error(checkResult.getError());
        }

        checkTaskId = model.addTask(checkTaskString);
        LinkedHashSet<String> insertionPointSuccessors = model.findSuccessors(insertionPointModelId.get());
        if (!insertionPointSuccessors.isEmpty()) {
            String insertionPointSuccessor = insertionPointSuccessors.iterator().next();
            model.clearSuccessors(insertionPointModelId.get());
            model.addUnlabelledSequenceFlow(checkTaskId, insertionPointSuccessor);
        }

        model.addUnlabelledSequenceFlow(insertionPointModelId.get(), checkTaskId);
        sessionStateStore.setModel(model);
        return Result.ok(new InsertionPointFindResult(checkTaskId, true));
    }

    public record InsertionPointFindResult(String insertionPointId, boolean isANewTask) {}
}
