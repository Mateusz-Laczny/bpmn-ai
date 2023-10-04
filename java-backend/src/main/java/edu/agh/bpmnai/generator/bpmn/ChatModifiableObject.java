package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.openai.ChatCallableInterface;

public interface ChatModifiableObject<T> {

    ChatCallableInterface getChatCallableInterface();

    T getObjectInstance();
}
