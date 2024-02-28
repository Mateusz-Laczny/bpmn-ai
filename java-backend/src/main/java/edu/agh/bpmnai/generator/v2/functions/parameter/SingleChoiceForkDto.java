package edu.agh.bpmnai.generator.v2.functions.parameter;

import edu.agh.bpmnai.generator.v2.Description;
import jakarta.annotation.Nullable;

import java.util.List;

public record SingleChoiceForkDto(
        @Description("Retrospective summary of the current state of the model")
        RetrospectiveSummary retrospectiveSummary,
        @Description("Name of the whole fork")
        String elementName,
        @Description("Activity in which the condition is checked, that determines which activity will be executed next. Does not have to exist in the model. In the verb+object naming convention")
        String checkActivity,
        @Description("Model element, which will be the direct predecessor to added fork in the process flow. Must be an element name that exists in the model, or a special 'Start' activity, indicating the start of the process. Must be provided, if `checkActivity` does not yet exist in the model")
        @Nullable
        String predecessorElement,
        @Description("List of possible choices. In the verb+object naming convention.")
        List<String> activitiesToChooseFrom
) {
}
