package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.FinishAskingQuestionsFunction;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.FinishAskingQuestionsDto;
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
    public Result<String, String> executeCall(String callArgumentsJson) {
        Result<FinishAskingQuestionsDto, String> argumentsParsingResult =
                callArgumentsParser.parseArguments(callArgumentsJson, FinishAskingQuestionsDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        FinishAskingQuestionsDto callArguments = argumentsParsingResult.getValue();
        return Result.ok(callArguments.finalMessageToTheUser());
    }
}
