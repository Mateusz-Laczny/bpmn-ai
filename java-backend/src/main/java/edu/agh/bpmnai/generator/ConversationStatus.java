package edu.agh.bpmnai.generator;

enum ConversationStatus {
    NEW,
    IN_PROGRESS,
    FINISHED,
    ERROR_TOO_MANY_TOKENS_REQUESTED,
    UNHANDLED_ERROR
}
