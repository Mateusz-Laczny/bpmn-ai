package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.databind.JsonNode;
import edu.agh.bpmnai.generator.v2.session.SessionState;

public interface FunctionCallExecutor {

    String getFunctionName();

    FunctionCallResult executeCall(SessionState sessionState, String functionId, JsonNode callArguments);
}
