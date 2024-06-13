package edu.agh.bpmnai.generator.v2.functions;

import edu.agh.bpmnai.generator.v2.session.ImmutableSessionState;

public record FunctionCallResult(ImmutableSessionState updatedSessionState, String responseToModel) {
}
