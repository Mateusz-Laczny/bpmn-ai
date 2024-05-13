package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.FillInMissingDetailsInUserRequestFunction;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.FillInMissingDetailsParametersDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FillInMissingDetailsCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    @Autowired
    public FillInMissingDetailsCallExecutor(ToolCallArgumentsParser callArgumentsParser) {
        this.callArgumentsParser = callArgumentsParser;
    }

    @Override
    public String getFunctionName() {
        return FillInMissingDetailsInUserRequestFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson) {
        Result<FillInMissingDetailsParametersDto, String> argumentsParsingResult =
                callArgumentsParser.parseArguments(callArgumentsJson, FillInMissingDetailsParametersDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        FillInMissingDetailsParametersDto callArguments = argumentsParsingResult.getValue();
        return Result.ok(callArguments.updatedRequest());
    }
}
