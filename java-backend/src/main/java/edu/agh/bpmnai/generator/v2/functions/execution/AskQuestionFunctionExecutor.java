package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.v2.functions.ArgumentsParsingResult;
import edu.agh.bpmnai.generator.v2.functions.AskQuestionFunction;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.AskQuestionFunctionParametersDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AskQuestionFunctionExecutor implements FunctionCallExecutor {
    private final ToolCallArgumentsParser callArgumentsParser;

    @Autowired
    public AskQuestionFunctionExecutor(ToolCallArgumentsParser callArgumentsParser) {
        this.callArgumentsParser = callArgumentsParser;
    }

    @Override
    public String getFunctionName() {
        return AskQuestionFunction.FUNCTION_NAME;
    }

    @Override
    public FunctionCallResult executeCall(String callArgumentsJson) {
        ArgumentsParsingResult<AskQuestionFunctionParametersDto> argumentsParsingResult =
                callArgumentsParser.parseArguments(callArgumentsJson, AskQuestionFunctionParametersDto.class);
        if (argumentsParsingResult.isError()) {
            return FunctionCallResult.unsuccessfulCall(argumentsParsingResult.errors());
        }

        AskQuestionFunctionParametersDto callArguments = argumentsParsingResult.result();
        return FunctionCallResult.withMessageToUser(callArguments.messageToTheUser());
    }
}
