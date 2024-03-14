package edu.agh.bpmnai.generator.v2.functions.parameter;

import jakarta.annotation.Nullable;

import java.util.List;

public record XorGatewayDto(
        @Description("Retrospective summary of the current state of the diagram")
        RetrospectiveSummary retrospectiveSummary,
        @Description("What is this action trying to achieve? Example: \"The customer can choose which mode of transport they prefer. There are 4 possible modes of transport, so I will add a single choice fork, which includes those choices\"")
        String reasoning,
        @Description("Name of the element")
        String elementName,
        @Description("Activity in which the condition is checked, that determines which activity be executed next. If does not exist currently in the diagram, it will be added. In the verb+object naming convention, should be a question like 'Pizza ok?'")
        String checkActivity,
        @Description("Diagram element, which will be the direct predecessor to added gateway in the process flow. Must be an element name that exists in the diagram, or a special 'Start' element, indicating the start of the process. Must be provided, if `checkActivity` does not yet exist in the diagram")
        @Nullable
        String predecessorElement,
        @Description("Activities, which will be added inside the gateway. In the verb+object naming convention. For the gateway to make sense it must contain at least 2 activities.")
        List<String> activitiesInsideGateway
) {
}
