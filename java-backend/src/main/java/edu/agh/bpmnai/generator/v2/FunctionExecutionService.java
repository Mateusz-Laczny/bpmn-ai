package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import edu.agh.bpmnai.generator.v2.functions.execution.FunctionCallExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public Optional<FunctionCallResult> executeFunctionCall(ToolCallDto functionCall) {
        String calledFunctionName = functionCall.functionCallProperties().name();
        if (!functionNameToExecutor.containsKey(calledFunctionName)) {
            log.warn("Could not find executor for function with name '{}'", calledFunctionName);
            return Optional.empty();
        }
        FunctionCallExecutor executorFunction = functionNameToExecutor.get(calledFunctionName);
        return Optional.of(executorFunction.executeCall(functionCall.functionCallProperties().argumentsJson()));
    }
}
