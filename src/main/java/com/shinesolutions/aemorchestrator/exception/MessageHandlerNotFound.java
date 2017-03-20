package com.shinesolutions.aemorchestrator.exception;

public class MessageHandlerNotFound extends Exception {

    private static final long serialVersionUID = 1L;

    public MessageHandlerNotFound(String messageSubject) {
        super("No message handler found for message with subject: " + messageSubject);
    }

}
