package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.ChatMessageDto;
import edu.agh.bpmnai.generator.v2.FunctionCallResponseDto;
import edu.agh.bpmnai.generator.v2.ParallelForkDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.BpmnGatewayType.INCLUSIVE;

@Service
@Slf4j
public class AddParallelActivitiesForkCallExecutor implements FunctionCallExecutor {

    private final ObjectMapper objectMapper;

    @Autowired
    public AddParallelActivitiesForkCallExecutor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getFunctionName() {
        return "add_parallel_activities_fork";
    }

    @Override
    public FunctionCallResult executeCall(SessionState sessionState, String functionId, JsonNode callArguments) {
        ParallelForkDto arguments;
        try {
            arguments = objectMapper.readValue(callArguments.asText(), ParallelForkDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        BpmnModel model = sessionState.model();
        String predecessorElementId;
        if (arguments.predecessorElement().equals("Start")) {
            predecessorElementId = model.findStartEvents().iterator().next();
        } else {
            predecessorElementId = model.findTaskIdByName(arguments.predecessorElement()).get();
        }
        Set<String> predecessorElementSuccessorsBeforeModification = model.findSuccessors(predecessorElementId);
        model.clearSuccessors(predecessorElementId);

        String openingGatewayId = model.addGateway(INCLUSIVE);
        String closingGatewayId = model.addGateway(INCLUSIVE);
        model.addUnlabelledSequenceFlow(predecessorElementId, openingGatewayId);
        for (String taskToExecute : arguments.activitiesToExecute()) {
            String taskId = model.addTask(taskToExecute);
            model.addUnlabelledSequenceFlow(openingGatewayId, taskId);
            model.addUnlabelledSequenceFlow(taskId, closingGatewayId);
        }

        if (!predecessorElementSuccessorsBeforeModification.isEmpty()) {
            if (predecessorElementSuccessorsBeforeModification.size() > 1) {
                log.warn("Predecessor element has more than one successor, choosing the first one; activityName: {}", arguments.predecessorElement());
            }

            String endOfChainElementId = predecessorElementSuccessorsBeforeModification.iterator().next();
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
