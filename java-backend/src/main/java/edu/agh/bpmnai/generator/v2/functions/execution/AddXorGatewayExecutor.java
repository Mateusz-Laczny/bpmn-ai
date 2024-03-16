package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.AddXorGatewayFunction;
import edu.agh.bpmnai.generator.v2.functions.ArgumentsParsingResult;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.XorGatewayDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
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

    private final SessionStateStore sessionStateStore;

    @Autowired
    public AddXorGatewayExecutor(ToolCallArgumentsParser callArgumentsParser, SessionStateStore sessionStateStore) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
    }

    @Override
    public String getFunctionName() {
        return AddXorGatewayFunction.FUNCTION_NAME;
    }

    @Override
    public FunctionCallResult executeCall(String callArgumentsJson) {
        ArgumentsParsingResult<XorGatewayDto> argumentsParsingResult = callArgumentsParser.parseArguments(callArgumentsJson, XorGatewayDto.class);
        if (argumentsParsingResult.isError()) {
            return FunctionCallResult.unsuccessfulCall(argumentsParsingResult.errors());
        }

        XorGatewayDto callArguments = argumentsParsingResult.result();
        BpmnModel model = sessionStateStore.model();
        String checkTaskName = callArguments.checkActivity();
        Optional<String> optionalTaskElementId = model.findElementByName(checkTaskName);
        String checkTaskId;
        Set<String> predecessorTaskSuccessorsBeforeModification;
        if (optionalTaskElementId.isPresent()) {
            checkTaskId = optionalTaskElementId.get();
            predecessorTaskSuccessorsBeforeModification = model.findSuccessors(checkTaskId);
        } else {
            Optional<String> optionalPredecessorElementId = model.findElementByName(callArguments.predecessorElement());
            if (optionalPredecessorElementId.isEmpty()) {
                log.info("Predecessor element does not exist in the model");
                return FunctionCallResult.unsuccessfulCall(List.of("Predecessor element does not exist in the model"));
            }
            String predecessorElementId = optionalPredecessorElementId.get();
            predecessorTaskSuccessorsBeforeModification = model.findSuccessors(predecessorElementId);
            model.clearSuccessors(predecessorElementId);
            checkTaskId = model.addTask(checkTaskName);
            model.addUnlabelledSequenceFlow(predecessorElementId, checkTaskId);
        }

        model.clearSuccessors(checkTaskId);

        String openingGatewayId = model.addGateway(EXCLUSIVE, callArguments.elementName() + " opening gateway");
        String closingGatewayId = model.addGateway(EXCLUSIVE, callArguments.elementName() + " closing gateway");
        model.addUnlabelledSequenceFlow(checkTaskId, openingGatewayId);

        for (String nextTaskPossibleChoice : callArguments.activitiesInsideGateway()) {
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
