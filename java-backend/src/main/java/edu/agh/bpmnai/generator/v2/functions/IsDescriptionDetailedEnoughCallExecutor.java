package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.functions.parameter.UserDescriptionReasoningDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IsDescriptionDetailedEnoughCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    @Autowired
    public IsDescriptionDetailedEnoughCallExecutor(ToolCallArgumentsParser callArgumentsParser) {
        this.callArgumentsParser = callArgumentsParser;
    }

    @Override
    public String getFunctionName() {
        return "is_request_detailed_enough";
    }

    @Override
    public FunctionCallResult executeCall(SessionState sessionState, String functionId, String callArgumentsJson) {
        ArgumentsParsingResult<UserDescriptionReasoningDto> argumentsParsingResult = callArgumentsParser.parseArguments(callArgumentsJson, UserDescriptionReasoningDto.class);
        if (argumentsParsingResult.isError()) {
            return FunctionCallResult.unsuccessfulCall(argumentsParsingResult.errors());
        }

        UserDescriptionReasoningDto callArguments = argumentsParsingResult.result();

        if (callArguments.messageToTheUser() != null && !callArguments.messageToTheUser().isEmpty()) {
            return FunctionCallResult.withMessageToUser(callArguments.messageToTheUser());
        }

        return FunctionCallResult.successfulCall();
    }
}
