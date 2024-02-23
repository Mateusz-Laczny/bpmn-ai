package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.v2.functions.FunctionCallExecutor;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public FunctionCallResult executeFunctionCall(SessionState sessionState, ToolCallDto functionCall) throws NoExecutorException {
        String calledFunctionName = functionCall.functionCallProperties().name();
        if (!functionNameToExecutor.containsKey(calledFunctionName)) {
            throw new NoExecutorException(calledFunctionName);
        }
        FunctionCallExecutor executorFunction = functionNameToExecutor.get(calledFunctionName);
        return executorFunction.executeCall(sessionState, functionCall.id(), functionCall.functionCallProperties().argumentsJson());
    }
}
