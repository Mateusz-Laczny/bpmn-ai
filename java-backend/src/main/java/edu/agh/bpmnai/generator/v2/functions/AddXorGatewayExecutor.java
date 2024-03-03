package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.parameter.XorGatewayDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;

@Service
@Slf4j
public class AddXorGatewayExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    @Autowired
    public AddXorGatewayExecutor(ToolCallArgumentsParser callArgumentsParser) {
        this.callArgumentsParser = callArgumentsParser;
    }

    @Override
    public String getFunctionName() {
        return AddXorGatewayFunction.FUNCTION_NAME;
    }

    @Override
    public FunctionCallResult executeCall(SessionState sessionState, String functionId, String callArgumentsJson) {
        ArgumentsParsingResult<XorGatewayDto> argumentsParsingResult = callArgumentsParser.parseArguments(callArgumentsJson, XorGatewayDto.class);
        if (argumentsParsingResult.isError()) {
            return FunctionCallResult.unsuccessfulCall(argumentsParsingResult.errors());
        }

        XorGatewayDto callArguments = argumentsParsingResult.result();
        BpmnModel model = sessionState.model();
        String checkTaskName = callArguments.checkTask();
        Optional<String> optionalTaskElementId = model.findTaskIdByName(checkTaskName);
        String checkTaskId;
        Set<String> predecessorTaskSuccessorsBeforeModification;
        if (optionalTaskElementId.isPresent()) {
            checkTaskId = optionalTaskElementId.get();
            predecessorTaskSuccessorsBeforeModification = model.findSuccessors(checkTaskId);
        } else {
            Optional<String> optionalPredecessorElementId = model.findTaskIdByName(callArguments.predecessorElement());
            if (optionalPredecessorElementId.isEmpty()) {
                log.info("Predecessor element does not exist in the model");
                return FunctionCallResult.unsuccessfulCall(List.of("Predecessor element does not exist in the model"));
            }
            String predecessorElementId = optionalPredecessorElementId.get();
            checkTaskId = model.addTask(checkTaskName);
            model.addUnlabelledSequenceFlow(predecessorElementId, checkTaskId);
            predecessorTaskSuccessorsBeforeModification = model.findSuccessors(predecessorElementId);
        }

        model.clearSuccessors(checkTaskId);

        String openingGatewayId = model.addGateway(EXCLUSIVE);
        String closingGatewayId = model.addGateway(EXCLUSIVE);
        model.addUnlabelledSequenceFlow(checkTaskId, openingGatewayId);

        for (String nextTaskPossibleChoice : callArguments.tasksToChooseFrom()) {
            String newTaskId = model.addTask(nextTaskPossibleChoice);
            model.addUnlabelledSequenceFlow(openingGatewayId, newTaskId);
            model.addUnlabelledSequenceFlow(newTaskId, closingGatewayId);
        }

        if (!predecessorTaskSuccessorsBeforeModification.isEmpty()) {
            if (predecessorTaskSuccessorsBeforeModification.size() > 1) {
                log.warn("Predecessor element has more than one successor, choosing the first one");
            }

            String endOfChainElementId = predecessorTaskSuccessorsBeforeModification.iterator().next();
            model.addUnlabelledSequenceFlow(closingGatewayId, endOfChainElementId);
        }

        model.setAlias(closingGatewayId, callArguments.elementName());
        return FunctionCallResult.successfulCall();
    }
}
