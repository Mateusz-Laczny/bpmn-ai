package edu.agh.bpmnai.generator.v2.session;

public enum SessionStatus {
    END,
    ASK_QUESTIONS,
    DECIDE_WHETHER_TO_MODIFY_THE_MODEL,
    MODIFY_MODEL,
    REASON_ABOUT_TASKS_AND_PROCESS_FLOW,
    FIX_ERRORS
}
