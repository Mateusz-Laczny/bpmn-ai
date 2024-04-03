package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.AddXorGatewayFunction;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
import edu.agh.bpmnai.generator.v2.functions.parameter.XorGatewayDto;
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
public class AddXorGatewayCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    private final ActivityService activityService;

    @Autowired
    public AddXorGatewayCallExecutor(
            ToolCallArgumentsParser callArgumentsParser,
            SessionStateStore sessionStateStore,
            ActivityService activityService
    ) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
        this.activityService = activityService;
    }

    @Override
    public String getFunctionName() {
        return AddXorGatewayFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, List<String>> executeCall(String callArgumentsJson) {
        Result<XorGatewayDto, List<String>> argumentsParsingResult = callArgumentsParser.parseArguments(
                callArgumentsJson,
                XorGatewayDto.class
        );
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        XorGatewayDto callArguments = argumentsParsingResult.getValue();

        if (callArguments.activitiesInsideGateway().size() < 2) {
            return Result.error(List.of("A gateway must contain at least 2 activities"));
        }

        BpmnModel model = sessionStateStore.model();
        String checkTaskName = callArguments.checkActivity();
        Optional<String> optionalTaskElementId = model.findElementByModelFriendlyId(checkTaskName);
        String checkTaskId;
        Set<String> predecessorTaskSuccessorsBeforeModification;
        if (optionalTaskElementId.isPresent()) {
            checkTaskId = optionalTaskElementId.get();
            predecessorTaskSuccessorsBeforeModification = model.findSuccessors(checkTaskId);
        } else {
            Optional<String> optionalPredecessorElementId =
                    model.findElementByModelFriendlyId(callArguments.predecessorElement());
            if (optionalPredecessorElementId.isEmpty()) {
                log.info("Predecessor element does not exist in the model");
                return Result.error(List.of("Predecessor element '%s' does not exist in the model".formatted(
                        callArguments.predecessorElement())));
            }
            String predecessorElementId = optionalPredecessorElementId.get();
            predecessorTaskSuccessorsBeforeModification = model.findSuccessors(predecessorElementId);
            model.clearSuccessors(predecessorElementId);
            checkTaskId = model.addTask(checkTaskName, checkTaskName);
            model.addUnlabelledSequenceFlow(predecessorElementId, checkTaskId);
        }

        model.clearSuccessors(checkTaskId);

        String openingGatewayId = model.addGateway(EXCLUSIVE, callArguments.elementName() + " opening gateway");
        String closingGatewayId = model.addGateway(EXCLUSIVE, callArguments.elementName() + " closing gateway");
        model.addUnlabelledSequenceFlow(checkTaskId, openingGatewayId);

        Set<String> addedActivitiesNames = new HashSet<>();
        for (Activity activityInsideGateway : callArguments.activitiesInsideGateway()) {
            Result<ActivityIdAndName, String> resultOfAddingActivity = activityService.addActivityToModel(
                    model,
                    activityInsideGateway
            );
            if (resultOfAddingActivity.isError()) {
                return Result.error(List.of(resultOfAddingActivity.getError()));
            }

            addedActivitiesNames.add(resultOfAddingActivity.getValue().modelFacingName());
            String activityId = resultOfAddingActivity.getValue().id();
            model.addUnlabelledSequenceFlow(openingGatewayId, activityId);
            model.addUnlabelledSequenceFlow(activityId, closingGatewayId);
        }

        if (!predecessorTaskSuccessorsBeforeModification.isEmpty()) {
            if (predecessorTaskSuccessorsBeforeModification.size() > 1) {
                log.warn("Predecessor element has more than one successor, choosing the first one");
            }

            String endOfChainElementId = predecessorTaskSuccessorsBeforeModification.iterator().next();
            model.addUnlabelledSequenceFlow(closingGatewayId, endOfChainElementId);
        }

        model.setAlias(closingGatewayId, callArguments.elementName());
        return Result.ok("Added activities: " + addedActivitiesNames);
    }
}
