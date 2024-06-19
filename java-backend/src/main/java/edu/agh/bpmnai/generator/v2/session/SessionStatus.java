package edu.agh.bpmnai.generator.v2.session;

public enum SessionStatus {
    NEW,
    PROMPTING_FINISHED_OK,
    PROMPTING_FINISHED_ERROR,
    DECIDE_WHETHER_TO_MODIFY_THE_MODEL,
    MODIFY_MODEL,
    REASON_ABOUT_TASKS_AND_PROCESS_FLOW;

    public boolean isFinishedStatus() {
        return this == PROMPTING_FINISHED_OK || this == PROMPTING_FINISHED_ERROR;
    }
}
