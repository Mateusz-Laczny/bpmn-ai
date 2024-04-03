package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.stream.Collectors.toSet;

@Service
public class ActivityService {

    private static Result<ActivityIdAndName, String> useExistingActivityInstanceIfExistsOhterwiseAddNew(
            BpmnModel model,
            Activity activity
    ) {
        Optional<String> elementId = model.findElementByModelFriendlyId(activity.activityName());
        if (elementId.isEmpty()) {
            String activityId = model.addTask(activity.activityName(), activity.activityName());
            return Result.ok(new ActivityIdAndName(activityId, activity.activityName()));
        }

        return Result.ok(new ActivityIdAndName(elementId.get(), activity.activityName()));
    }

    private static Result<ActivityIdAndName, String> addNewActivity(
            BpmnModel model,
            Activity activity
    ) {
        String modelFacingName = activity.activityName();
        Optional<String> elementId = model.findElementByModelFriendlyId(modelFacingName);
        if (elementId.isPresent()) {
            modelFacingName = "%s (after %s)".formatted(
                    modelFacingName,
                    model.findPredecessors(elementId.get())
                            .stream()
                            .map(model::getModelFriendlyId)
                            .collect(toSet())
            );
        }

        String activityId = model.addTask(activity.activityName(), modelFacingName);
        return Result.ok(new ActivityIdAndName(activityId, modelFacingName));
    }

    public Result<ActivityIdAndName, String> addActivityToModel(BpmnModel model, Activity activity) {
        Result<ActivityIdAndName, String> addActivityResult;
        switch (activity.howToHandleDuplicates()) {
            case ADD_NEW_INSTANCE -> addActivityResult = addNewActivity(model, activity);
            case USE_EXISTING -> addActivityResult = useExistingActivityInstanceIfExistsOhterwiseAddNew(
                    model,
                    activity
            );
            default ->
                    throw new IllegalStateException("Unexpected strategy '%s'".formatted(activity.howToHandleDuplicates()));
        }

        if (addActivityResult.isError()) {
            return addActivityResult;
        }

        if (activity.isProcessEnd()) {
            String activityId = addActivityResult.getValue().id();
            String endEventId = model.addEndEvent();
            model.addUnlabelledSequenceFlow(activityId, endEventId);
        }

        return addActivityResult;
    }
}
