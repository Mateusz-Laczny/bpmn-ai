package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.RespondWithoutModifyingDiagramFunction;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.RespondWithoutModifyingDiagramParametersDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RespondWithoutModifyingDiagramFunctionCallExecutor implements FunctionCallExecutor {
    private final ToolCallArgumentsParser callArgumentsParser;

    @Autowired
    public RespondWithoutModifyingDiagramFunctionCallExecutor(ToolCallArgumentsParser callArgumentsParser) {
        this.callArgumentsParser = callArgumentsParser;
    }

    @Override
    public String getFunctionName() {
        return RespondWithoutModifyingDiagramFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson) {
        Result<RespondWithoutModifyingDiagramParametersDto, String> argumentsParsingResult =
                callArgumentsParser.parseArguments(
                        callArgumentsJson,
                        RespondWithoutModifyingDiagramParametersDto.class
                );
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        RespondWithoutModifyingDiagramParametersDto callArguments = argumentsParsingResult.getValue();
        return Result.ok(callArguments.messageToTheUser());

    }
}
