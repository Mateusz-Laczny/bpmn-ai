package edu.agh.bpmnai.generator.v2.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionStatusTest {

    @Test
    void prompting_finished_ok_is_finished_status() {
        assertTrue(SessionStatus.PROMPTING_FINISHED_OK.isFinishedStatus());
    }

    @Test
    void prompting_finished_error_is_finished_status() {
        assertTrue(SessionStatus.PROMPTING_FINISHED_ERROR.isFinishedStatus());
    }

    @Test
    void new_is_not_finished_status() {
        assertFalse(SessionStatus.NEW.isFinishedStatus());
    }
}