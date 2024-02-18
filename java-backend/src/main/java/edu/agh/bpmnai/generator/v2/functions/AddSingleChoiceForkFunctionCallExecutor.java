package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import edu.agh.bpmnai.generator.v2.FunctionCallResponseDto;
import edu.agh.bpmnai.generator.v2.SingleChoiceForkDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.EXCLUSIVE;

@Service
@Slf4j
public class AddSingleChoiceForkFunctionCallExecutor implements FunctionCallExecutor {

    private final ObjectMapper objectMapper;

    @Autowired
    public AddSingleChoiceForkFunctionCallExecutor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getFunctionName() {
        return "add_single_choice_fork_between_activities";
    }

    @Override
    public FunctionCallResult executeCall(SessionState sessionState, String functionId, JsonNode callArguments) {
        SingleChoiceForkDto arguments;
        try {
            arguments = objectMapper.readValue(callArguments.asText(), SingleChoiceForkDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        BpmnModel model = sessionState.model();
        String checkActivityName = arguments.checkActivity();
        Optional<String> optionalCheckActivityElementId = model.findTaskIdByName(checkActivityName);
        String checkTaskId;
        Set<String> predecessorTaskSuccessorsBeforeModification;
        if (optionalCheckActivityElementId.isPresent()) {
            checkTaskId = optionalCheckActivityElementId.get();
            predecessorTaskSuccessorsBeforeModification = model.findSuccessors(checkTaskId);
        } else {
            String predecessorElementId;
            if (arguments.predecessorElement() == null || arguments.predecessorElement().equals("Start")) {
                predecessorElementId = model.findStartEvents().iterator().next();
            } else {
                predecessorElementId = model.findTaskIdByName(arguments.predecessorElement()).get();
            }
            checkTaskId = model.addTask(checkActivityName);
            model.addUnlabelledSequenceFlow(predecessorElementId, checkTaskId);
            predecessorTaskSuccessorsBeforeModification = model.findSuccessors(predecessorElementId);
        }

        model.clearSuccessors(checkTaskId);

        String openingGatewayId = model.addGateway(EXCLUSIVE);
        String closingGatewayId = model.addGateway(EXCLUSIVE);
        model.addUnlabelledSequenceFlow(checkTaskId, openingGatewayId);

        for (String nextTaskPossibleChoice : arguments.activitiesToChooseFrom()) {
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

        model.setAlias(closingGatewayId, arguments.elementName());
        try {
            String responseContent = objectMapper.writeValueAsString(new FunctionCallResponseDto(true));
            return FunctionCallResult.withResponse(new ChatMessageDto("tool", responseContent, functionId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
