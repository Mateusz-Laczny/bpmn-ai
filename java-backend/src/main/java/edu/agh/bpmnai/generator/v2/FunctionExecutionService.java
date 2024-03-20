package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.execution.FunctionCallExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.agh.bpmnai.generator.v2.CallErrorType.CALL_FAILED;
import static edu.agh.bpmnai.generator.v2.CallErrorType.NO_EXECUTOR_FOUND;

@Service
@Slf4j
public class FunctionExecutionService {

    private final Map<String, FunctionCallExecutor> functionNameToExecutor;

    public FunctionExecutionService(List<FunctionCallExecutor> functionCallExecutors) {
        functionNameToExecutor = new HashMap<>();
        for (FunctionCallExecutor functionCallExecutor : functionCallExecutors) {
            functionNameToExecutor.put(functionCallExecutor.getFunctionName(), functionCallExecutor);
        }
    }

    public Result<String, CallError> executeFunctionCall(ToolCallDto functionCall) {
        String calledFunctionName = functionCall.functionCallProperties().name();
        if (!functionNameToExecutor.containsKey(calledFunctionName)) {
            return Result.error(new CallError(NO_EXECUTOR_FOUND, "No executor for function with name '%s'".formatted(calledFunctionName)));
        }
        FunctionCallExecutor executorFunction = functionNameToExecutor.get(calledFunctionName);
        Result<String, List<String>> callResult = executorFunction.executeCall(functionCall.functionCallProperties().argumentsJson());
        if (callResult.isError()) {
            return Result.error(new CallError(CALL_FAILED, callResult.getError().toString()));
        }

        return Result.ok(callResult.getValue());
    }
}
