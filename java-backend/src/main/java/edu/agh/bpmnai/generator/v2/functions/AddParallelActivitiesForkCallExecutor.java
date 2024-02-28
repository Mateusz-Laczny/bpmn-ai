package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.parameter.ParallelForkDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.INCLUSIVE;

@Service
@Slf4j
public class AddParallelActivitiesForkCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    @Autowired
    public AddParallelActivitiesForkCallExecutor(ToolCallArgumentsParser callArgumentsParser) {
        this.callArgumentsParser = callArgumentsParser;
    }

    @Override
    public String getFunctionName() {
        return "add_parallel_activities_fork";
    }

    @Override
    public FunctionCallResult executeCall(SessionState sessionState, String functionId, String callArgumentsJson) {
        ArgumentsParsingResult<ParallelForkDto> argumentsParsingResult = callArgumentsParser.parseArguments(callArgumentsJson, ParallelForkDto.class);
        if (argumentsParsingResult.isError()) {
            return FunctionCallResult.unsuccessfulCall(argumentsParsingResult.errors());
        }

        ParallelForkDto callArguments = argumentsParsingResult.result();

        BpmnModel model = sessionState.model();
        Optional<String> optionalPredecessorElementId = model.findTaskIdByName(callArguments.predecessorElement());
        if (optionalPredecessorElementId.isEmpty()) {
            log.info("Predecessor element does not exist in the model");
            return FunctionCallResult.unsuccessfulCall(List.of("Predecessor element does not exist in the model"));
        }

        String predecessorElementId = optionalPredecessorElementId.get();

        Set<String> predecessorElementSuccessorsBeforeModification = model.findSuccessors(predecessorElementId);
        model.clearSuccessors(predecessorElementId);

        String openingGatewayId = model.addGateway(INCLUSIVE);
        String closingGatewayId = model.addGateway(INCLUSIVE);
        model.addUnlabelledSequenceFlow(predecessorElementId, openingGatewayId);
        for (String taskToExecute : callArguments.activitiesToExecute()) {
            String taskId = model.addTask(taskToExecute);
            model.addUnlabelledSequenceFlow(openingGatewayId, taskId);
            model.addUnlabelledSequenceFlow(taskId, closingGatewayId);
        }

        if (!predecessorElementSuccessorsBeforeModification.isEmpty()) {
            if (predecessorElementSuccessorsBeforeModification.size() > 1) {
                log.warn("Predecessor element has more than one successor, choosing the first one; activityName: {}", callArguments.predecessorElement());
            }

            String endOfChainElementId = predecessorElementSuccessorsBeforeModification.iterator().next();
            model.addUnlabelledSequenceFlow(closingGatewayId, endOfChainElementId);
        }

        model.setAlias(closingGatewayId, callArguments.elementName());
        return FunctionCallResult.successfulCall();
    }
}
