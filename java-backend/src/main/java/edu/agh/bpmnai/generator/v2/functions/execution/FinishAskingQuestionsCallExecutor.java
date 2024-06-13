package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.FinishAskingQuestionsFunction;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.FinishAskingQuestionsDto;
import edu.agh.bpmnai.generator.v2.session.ImmutableSessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FinishAskingQuestionsCallExecutor implements FunctionCallExecutor {
    private final ToolCallArgumentsParser callArgumentsParser;

    @Autowired
    public FinishAskingQuestionsCallExecutor(ToolCallArgumentsParser callArgumentsParser) {
        this.callArgumentsParser = callArgumentsParser;
    }

    @Override
    public String getFunctionName() {
        return FinishAskingQuestionsFunction.FUNCTION_NAME;
    }

    @Override
    public Result<FunctionCallResult, String> executeCall(
            String callArgumentsJson,
            ImmutableSessionState sessionState
    ) {
        Result<FinishAskingQuestionsDto, String> argumentsParsingResult =
                callArgumentsParser.parseArguments(callArgumentsJson, FinishAskingQuestionsDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        FinishAskingQuestionsDto callArguments = argumentsParsingResult.getValue();
        return Result.ok(new FunctionCallResult(sessionState, callArguments.finalMessageToTheUser()));
    }
}
