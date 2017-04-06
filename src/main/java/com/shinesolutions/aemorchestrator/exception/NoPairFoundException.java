package com.shinesolutions.aemorchestrator.exception;

public class NoPairFoundException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public NoPairFoundException(String instanceId) {
        super("Unable to find available pair for instance " + instanceId);
    }
}
