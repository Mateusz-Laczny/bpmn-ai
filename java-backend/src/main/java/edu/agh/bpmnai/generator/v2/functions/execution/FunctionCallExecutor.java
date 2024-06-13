package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.FunctionCallResult;
import edu.agh.bpmnai.generator.v2.session.ImmutableSessionState;

public interface FunctionCallExecutor {

    String getFunctionName();

    Result<FunctionCallResult, String> executeCall(String callArguments, ImmutableSessionState sessionState);
}
