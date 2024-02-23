package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.session.SessionState;

public interface FunctionCallExecutor {

    String getFunctionName();

    FunctionCallResult executeCall(SessionState sessionState, String functionId, String callArguments);
}
