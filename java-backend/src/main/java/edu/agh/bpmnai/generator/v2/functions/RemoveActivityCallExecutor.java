package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveActivityDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RemoveActivityCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    @Autowired
    public RemoveActivityCallExecutor(ToolCallArgumentsParser callArgumentsParser) {
        this.callArgumentsParser = callArgumentsParser;
    }

    @Override
    public String getFunctionName() {
        return "remove_activity";
    }

    @Override
    public FunctionCallResult executeCall(SessionState sessionState, String functionId, String callArgumentsJson) {
        ArgumentsParsingResult<RemoveActivityDto> argumentsParsingResult = callArgumentsParser.parseArguments(callArgumentsJson, RemoveActivityDto.class);
        if (argumentsParsingResult.isError()) {
            return FunctionCallResult.unsuccessfulCall(argumentsParsingResult.errors());
        }

        RemoveActivityDto callArguments = argumentsParsingResult.result();

        BpmnModel model = sessionState.model();
        Optional<String> elementToCutOutId = model.findTaskIdByName(callArguments.activityToRemove());

        elementToCutOutId.ifPresent(model::cutOutElement);
        return FunctionCallResult.successfulCall();
    }
}
