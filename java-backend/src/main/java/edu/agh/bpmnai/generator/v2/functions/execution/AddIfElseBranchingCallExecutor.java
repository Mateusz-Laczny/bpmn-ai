package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.AddIfElseBranchingFunction;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.IfElseBranchingDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;

@Service
@Slf4j
public class AddIfElseBranchingCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    private final ActivityService activityService;

    @Autowired
    public AddIfElseBranchingCallExecutor(ToolCallArgumentsParser callArgumentsParser, SessionStateStore sessionStateStore, ActivityService activityService) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
        this.activityService = activityService;
    }

    @Override
    public String getFunctionName() {
        return AddIfElseBranchingFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, List<String>> executeCall(String callArgumentsJson) {
        Result<IfElseBranchingDto, List<String>> argumentsParsingResult = callArgumentsParser.parseArguments(callArgumentsJson, IfElseBranchingDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        IfElseBranchingDto callArguments = argumentsParsingResult.getValue();
        BpmnModel model = sessionStateStore.model();
        String checkTaskName = callArguments.checkTask();
        Optional<String> optionalCheckTaskElementId = model.findElementByModelFriendlyId(checkTaskName);
        String checkTaskElementId;
        Set<String> addedActivitiesNames = new HashSet<>();
        if (optionalCheckTaskElementId.isPresent()) {
            checkTaskElementId = optionalCheckTaskElementId.get();
        } else {
            if (callArguments.predecessorElement() == null) {
                log.info("Check task does not exist in the model and predecessor element is null, tool call cannot proceed");
                return Result.error(List.of("Check task does not exist in the model and predecessor element is null"));
            }

            Optional<String> predecessorElementId = model.findElementByModelFriendlyId(callArguments.predecessorElement());
            if (predecessorElementId.isEmpty()) {
                log.info("Predecessor element does not exist in the model");
                return Result.error(List.of("Predecessor element does not exist in the model"));
            }

            String previousElementId = predecessorElementId.get();

            String checkTaskId = model.addTask(checkTaskName, checkTaskName);
            addedActivitiesNames.add(checkTaskName);
            model.addUnlabelledSequenceFlow(previousElementId, checkTaskId);
            checkTaskElementId = checkTaskId;
        }

        model.clearSuccessors(checkTaskElementId);

        Result<ActivityIdAndName, String> trueBranchBeginningAddResult = activityService.addActivityToModel(model, callArguments.trueBranchBeginningTask());
        if (trueBranchBeginningAddResult.isError()) {
            return Result.error(List.of(trueBranchBeginningAddResult.getError()));
        }

        String trueBranchBeginningElementId = trueBranchBeginningAddResult.getValue().id();
        addedActivitiesNames.add(trueBranchBeginningAddResult.getValue().modelFacingName());

        Result<ActivityIdAndName, String> falseBranchBeginningAddResult = activityService.addActivityToModel(model, callArguments.falseBranchBeginningTask());
        if (falseBranchBeginningAddResult.isError()) {
            return Result.error(List.of(falseBranchBeginningAddResult.getError()));
        }

        String falseBranchBeginningElementId = falseBranchBeginningAddResult.getValue().id();
        addedActivitiesNames.add(trueBranchBeginningAddResult.getValue().modelFacingName());

        String gatewayId = model.addGateway(EXCLUSIVE, callArguments.elementName() + " gateway");
        model.addUnlabelledSequenceFlow(checkTaskElementId, gatewayId);
        model.addLabelledSequenceFlow(gatewayId, trueBranchBeginningElementId, "Yes");
        model.addLabelledSequenceFlow(gatewayId, falseBranchBeginningElementId, "No");

        return Result.ok("Added activities: " + addedActivitiesNames);
    }
}
