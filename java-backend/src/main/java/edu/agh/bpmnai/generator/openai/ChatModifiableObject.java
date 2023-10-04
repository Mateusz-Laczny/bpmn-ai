package edu.agh.bpmnai.generator.openai;

public interface ChatModifiableObject<T> {

    ChatCallableInterface getChatCallableInterface();

    T getObjectInstance();
}
