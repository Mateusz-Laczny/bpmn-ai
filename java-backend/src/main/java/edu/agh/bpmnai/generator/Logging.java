package edu.agh.bpmnai.generator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Logging {

    public static void logInfoMessage(String message, ObjectToLog... objectsToLog) {
        if (log.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append(message).append(';').append(' ');
            for (ObjectToLog objectToLog : objectsToLog) {
                sb.append(objectToLog.label()).append('=').append(objectToLog.object().toString()).append(", ");
            }

            log.info(sb.toString());
        }
    }

    public static void logThrowable(String message, Throwable throwable) {
        if (log.isErrorEnabled()) {
            log.error(message, throwable);
        }
    }

    public record ObjectToLog(String label, Object object) {

    }
}
