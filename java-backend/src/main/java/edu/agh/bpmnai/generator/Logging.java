package edu.agh.bpmnai.generator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Logging {

    private static String buildLogMessage(String message, ObjectToLog[] objectsToLog) {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append(';').append(' ');
        for (ObjectToLog objectToLog : objectsToLog) {
            sb.append(objectToLog.label()).append('=').append(objectToLog.object().toString()).append(", ");
        }
        return sb.toString();
    }

    public static void logDebugMessage(String message, ObjectToLog... objectsToLog) {
        if (log.isDebugEnabled()) {
            String logMessage = buildLogMessage(message, objectsToLog);
            log.debug(logMessage);
        }
    }

    public static void logInfoMessage(String message, ObjectToLog... objectsToLog) {
        if (log.isInfoEnabled()) {
            String logMessage = buildLogMessage(message, objectsToLog);
            log.info(logMessage);
        }
    }

    public static void logWarnMessage(String message, ObjectToLog... objectsToLog) {
        if (log.isWarnEnabled()) {
            String logMessage = buildLogMessage(message, objectsToLog);
            log.warn(logMessage);
        }
    }

    public static void logErrorMessage(String message, ObjectToLog... objectsToLog) {
        if (log.isErrorEnabled()) {
            String logMessage = buildLogMessage(message, objectsToLog);
            log.error(logMessage);
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
