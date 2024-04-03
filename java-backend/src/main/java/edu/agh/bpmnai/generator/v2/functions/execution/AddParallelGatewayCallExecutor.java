package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.AddParallelGatewayFunction;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.Activity;
import edu.agh.bpmnai.generator.v2.functions.parameter.ParallelGatewayDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.PARALLEL;

@Service
@Slf4j
public class AddParallelGatewayCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    private final ActivityService activityService;

    @Autowired
    public AddParallelGatewayCallExecutor(ToolCallArgumentsParser callArgumentsParser, SessionStateStore sessionStateStore, ActivityService activityService) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
        this.activityService = activityService;
    }

    @Override
    public String getFunctionName() {
        return AddParallelGatewayFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, List<String>> executeCall(String callArgumentsJson) {
        Result<ParallelGatewayDto, List<String>> argumentsParsingResult = callArgumentsParser.parseArguments(callArgumentsJson, ParallelGatewayDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        ParallelGatewayDto callArguments = argumentsParsingResult.getValue();

        if (callArguments.activitiesInsideGateway().size() < 2) {
            return Result.error(List.of("A gateway must contain at least 2 activities"));
        }

        BpmnModel model = sessionStateStore.model();
        Optional<String> optionalPredecessorElementId = model.findElementByModelFriendlyId(callArguments.predecessorElement());
        if (optionalPredecessorElementId.isEmpty()) {
            log.info("Predecessor element does not exist in the model");
            return Result.error(List.of("Predecessor element '%s' does not exist in the model".formatted(callArguments.predecessorElement())));
        }

        String predecessorElementId = optionalPredecessorElementId.get();
        Set<String> predecessorElementSuccessorsBeforeModification = model.findSuccessors(predecessorElementId);
        model.clearSuccessors(predecessorElementId);

        String openingGatewayId = model.addGateway(PARALLEL, callArguments.elementName() + " opening gateway");
        String closingGatewayId = model.addGateway(PARALLEL, callArguments.elementName() + " closing gateway");
        model.addUnlabelledSequenceFlow(predecessorElementId, openingGatewayId);

        Set<String> addedActivitiesNames = new HashSet<>();
        for (Activity activityInGateway : callArguments.activitiesInsideGateway()) {
            Result<ActivityIdAndName, String> activityAddResult = activityService.addActivityToModel(model, activityInGateway);
            if (activityAddResult.isError()) {
                return Result.error(List.of(activityAddResult.getError()));
            }

            String activityId = activityAddResult.getValue().id();
            addedActivitiesNames.add(activityAddResult.getValue().modelFacingName());
            model.addUnlabelledSequenceFlow(openingGatewayId, activityId);
            model.addUnlabelledSequenceFlow(activityId, closingGatewayId);
        }

        if (!predecessorElementSuccessorsBeforeModification.isEmpty()) {
            if (predecessorElementSuccessorsBeforeModification.size() > 1) {
                log.warn("Predecessor element has more than one successor, choosing the first one; activityName: {}", callArguments.predecessorElement());
            }

            String endOfChainElementId = predecessorElementSuccessorsBeforeModification.iterator().next();
            model.addUnlabelledSequenceFlow(closingGatewayId, endOfChainElementId);
        }

        model.setAlias(closingGatewayId, callArguments.elementName());
        return Result.ok("Added activities: " + addedActivitiesNames);
    }
}
