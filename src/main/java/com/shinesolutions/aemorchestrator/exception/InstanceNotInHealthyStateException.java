package com.shinesolutions.aemorchestrator.exception;

public class InstanceNotInHealthyStateException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public InstanceNotInHealthyStateException(String instanceId) {
        super(getMessage(instanceId));
    }
    
    public InstanceNotInHealthyStateException(String instanceId, Throwable cause) {
        super(getMessage(instanceId), cause);
    }
    
    private static String getMessage(String instanceId) {
        return "Instance with id " + instanceId + " is not responding to health checks within the designated time";
    }

}
