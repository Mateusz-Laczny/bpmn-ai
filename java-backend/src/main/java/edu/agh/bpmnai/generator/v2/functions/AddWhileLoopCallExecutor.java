package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.parameter.WhileLoopDto;
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
public class AddWhileLoopCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    @Autowired
    public AddWhileLoopCallExecutor(ToolCallArgumentsParser callArgumentsParser) {
        this.callArgumentsParser = callArgumentsParser;
    }

    @Override
    public String getFunctionName() {
        return "add_while_loop";
    }

    @Override
    public FunctionCallResult executeCall(SessionState sessionState, String functionId, String callArgumentsJson) {
        ArgumentsParsingResult<WhileLoopDto> argumentsParsingResult = callArgumentsParser.parseArguments(callArgumentsJson, WhileLoopDto.class);
        if (argumentsParsingResult.isError()) {
            return FunctionCallResult.unsuccessfulCall(argumentsParsingResult.errors());
        }

        WhileLoopDto callArguments = argumentsParsingResult.result();

        BpmnModel model = sessionState.model();
        String checkTaskName = callArguments.checkTask();
        Optional<String> optionalCheckTaskElementId = model.findTaskIdByName(checkTaskName);
        String checkTaskId;
        Set<String> predecessorTaskSuccessorsBeforeModification;
        if (optionalCheckTaskElementId.isPresent()) {
            checkTaskId = optionalCheckTaskElementId.get();
            predecessorTaskSuccessorsBeforeModification = model.findSuccessors(checkTaskId);
        } else {
            Optional<String> optionalPredecessorElementId = model.findTaskIdByName(callArguments.predecessorElement());
            if (optionalPredecessorElementId.isEmpty()) {
                log.info("Predecessor element does not exist in the model");
                return FunctionCallResult.unsuccessfulCall(List.of("Predecessor element does not exist in the model"));
            }
            String predecessorElementId = optionalPredecessorElementId.get();
            predecessorTaskSuccessorsBeforeModification = model.findSuccessors(predecessorElementId);
            checkTaskId = model.addTask(checkTaskName);
            model.addUnlabelledSequenceFlow(predecessorElementId, checkTaskId);
        }

        model.clearSuccessors(checkTaskId);

        String openingGatewayId = model.addGateway(EXCLUSIVE);
        model.addUnlabelledSequenceFlow(checkTaskId, openingGatewayId);
        if (!predecessorTaskSuccessorsBeforeModification.isEmpty()) {
            if (predecessorTaskSuccessorsBeforeModification.size() > 1) {
                log.warn("Predecessor activity has more than on successor, choosing the first one");
            }
            String nextTaskId = predecessorTaskSuccessorsBeforeModification.iterator().next();
            model.addLabelledSequenceFlow(openingGatewayId, nextTaskId, "false");
        }

        String previousElementInLoopId = openingGatewayId;
        for (String taskInLoop : callArguments.tasksInLoop()) {
            String newTaskId = model.addTask(taskInLoop);
            model.addUnlabelledSequenceFlow(previousElementInLoopId, newTaskId);
            previousElementInLoopId = newTaskId;
        }

        model.addUnlabelledSequenceFlow(previousElementInLoopId, checkTaskId);
        return FunctionCallResult.successfulCall();
    }
}
