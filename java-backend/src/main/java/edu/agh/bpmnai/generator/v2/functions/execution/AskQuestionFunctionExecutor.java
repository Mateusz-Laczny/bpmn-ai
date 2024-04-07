package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.AskQuestionFunction;
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
    public Result<String, String> executeCall(String callArgumentsJson, BpmnManagedReference modelReference) {
        Result<AskQuestionFunctionParametersDto, String> argumentsParsingResult =
                callArgumentsParser.parseArguments(callArgumentsJson, AskQuestionFunctionParametersDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        AskQuestionFunctionParametersDto callArguments = argumentsParsingResult.getValue();
        return Result.ok(callArguments.messageToTheUser());
    }
}
