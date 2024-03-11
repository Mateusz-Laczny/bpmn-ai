package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.AddParallelGatewayFunction;
import edu.agh.bpmnai.generator.v2.functions.ArgumentsParsingResult;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.ParallelGatewayDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.INCLUSIVE;

@Service
@Slf4j
public class AddParallelGatewayCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    @Autowired
    public AddParallelGatewayCallExecutor(ToolCallArgumentsParser callArgumentsParser, SessionStateStore sessionStateStore) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
    }

    @Override
    public String getFunctionName() {
        return AddParallelGatewayFunction.FUNCTION_NAME;
    }

    @Override
    public FunctionCallResult executeCall(String callArgumentsJson) {
        ArgumentsParsingResult<ParallelGatewayDto> argumentsParsingResult = callArgumentsParser.parseArguments(callArgumentsJson, ParallelGatewayDto.class);
        if (argumentsParsingResult.isError()) {
            return FunctionCallResult.unsuccessfulCall(argumentsParsingResult.errors());
        }

        ParallelGatewayDto callArguments = argumentsParsingResult.result();

        BpmnModel model = sessionStateStore.model();
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
        for (String taskToExecute : callArguments.tasksToExecute()) {
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
