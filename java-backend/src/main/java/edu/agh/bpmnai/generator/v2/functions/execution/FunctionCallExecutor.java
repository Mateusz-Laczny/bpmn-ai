package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.datatype.Result;

public interface FunctionCallExecutor {

    String getFunctionName();

    Result<String, String> executeCall(String callArguments);
}
