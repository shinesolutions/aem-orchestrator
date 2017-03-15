package com.shinesolutions.aemorchestrator.exception;

public class InstanceNotInHealthyState extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public InstanceNotInHealthyState(String instanceId) {
        super(getMessage(instanceId));
    }
    
    public InstanceNotInHealthyState(String instanceId, Throwable cause) {
        super(getMessage(instanceId), cause);
    }
    
    private static String getMessage(String instanceId) {
        return "Instance with id " + instanceId + " is not responding to health checks within the designated time";
    }

}
