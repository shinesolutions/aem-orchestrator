package com.shinesolutions.aemorchestrator.model;


public enum EventType {
    AUTOSCALING_EC2_INSTANCE_TERMINATE("Auto Scaling: termination"),
    AUTOSCALING_EC2_INSTANCE_LAUNCH("Auto Scaling: launch"),
    AUTOSCALING_TEST_NOTIFICATION("Auto Scaling: test"),
    ALARM("ALARM");
    
    private final String value;

    private EventType(String v) {
        value = v;
    }

    @Override
    public String toString() {
        return this.value;
    }
    
    public String getValue() {
        return this.value;
    }
}
