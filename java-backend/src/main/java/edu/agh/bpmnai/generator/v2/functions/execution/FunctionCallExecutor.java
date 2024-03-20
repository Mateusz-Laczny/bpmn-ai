package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.datatype.Result;

import java.util.List;

public interface FunctionCallExecutor {

    String getFunctionName();

    Result<String, List<String>> executeCall(String callArguments);
}
