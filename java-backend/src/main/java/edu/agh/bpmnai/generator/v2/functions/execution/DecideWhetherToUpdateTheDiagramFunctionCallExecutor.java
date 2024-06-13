package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.DecideWhetherToUpdateTheDiagramFunction;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.DecideWhetherToUpdateTheDiagramFunctionParameters;
import edu.agh.bpmnai.generator.v2.session.ImmutableSessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DecideWhetherToUpdateTheDiagramFunctionCallExecutor implements FunctionCallExecutor {
    private final ToolCallArgumentsParser callArgumentsParser;

    public DecideWhetherToUpdateTheDiagramFunctionCallExecutor(ToolCallArgumentsParser callArgumentsParser) {
        this.callArgumentsParser = callArgumentsParser;
    }

    @Override
    public String getFunctionName() {
        return DecideWhetherToUpdateTheDiagramFunction.FUNCTION_NAME;
    }

    @Override
    public Result<FunctionCallResult, String> executeCall(
            String callArgumentsJson,
            ImmutableSessionState sessionState
    ) {
        Result<DecideWhetherToUpdateTheDiagramFunctionParameters, String> argumentsParsingResult =
                callArgumentsParser.parseArguments(
                        callArgumentsJson,
                        DecideWhetherToUpdateTheDiagramFunctionParameters.class
                );

        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        DecideWhetherToUpdateTheDiagramFunctionParameters callArguments = argumentsParsingResult.getValue();
        if (callArguments.diagramNeedsUpdate()) {
            return Result.ok(new FunctionCallResult(sessionState, ""));
        }

        return Result.ok(new FunctionCallResult(sessionState, callArguments.finalMessage()));
    }
}
