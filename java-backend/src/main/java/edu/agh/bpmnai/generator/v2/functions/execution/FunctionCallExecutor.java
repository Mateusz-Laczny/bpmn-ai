package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;

public interface FunctionCallExecutor {

    String getFunctionName();

    FunctionCallResult executeCall(String callArguments);
}
