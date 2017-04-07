package com.shinesolutions.aemorchestrator.exception;

public class MessageHandlerNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public MessageHandlerNotFoundException(String messageSubject) {
        super("No message handler found for message with subject: " + messageSubject);
    }

}
