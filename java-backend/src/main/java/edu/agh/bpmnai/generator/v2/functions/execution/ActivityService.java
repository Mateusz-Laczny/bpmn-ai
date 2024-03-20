package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ActivityService {

    public Result<ActivityIdAndName, String> addActivityToModel(BpmnModel model, Activity activity) {
        switch (activity.howToHandleDuplicates()) {
            case ADD_NEW_INSTANCE -> {
                String modelFacingName = activity.activityName();
                int numberOfCollisions = 0;
                while (model.findElementByModelFriendlyId(modelFacingName).isPresent()) {
                    numberOfCollisions += 1;
                    modelFacingName = "%s (%d)".formatted(modelFacingName, numberOfCollisions);
                }

                String activityId = model.addTask(activity.activityName(), modelFacingName);
                return Result.ok(new ActivityIdAndName(activityId, modelFacingName));
            }
            case USE_EXISTING -> {
                Optional<String> elementId = model.findElementByModelFriendlyId(activity.activityName());
                if (elementId.isEmpty()) {
                    String activityId = model.addTask(activity.activityName(), activity.activityName());
                    return Result.ok(new ActivityIdAndName(activityId, activity.activityName()));
                }

                return Result.ok(new ActivityIdAndName(elementId.get(), activity.activityName()));
            }
            default ->
                    throw new IllegalStateException("Unexpected strategy '%s'".formatted(activity.howToHandleDuplicates()));
        }
    }
}
