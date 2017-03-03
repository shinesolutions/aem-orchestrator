package com.shinesolutions.aemorchestrator.model;

public enum EventType {
    AUTOSCALING_EC2_INSTANCE_TERMINATE("autoscaling:EC2_INSTANCE_TERMINATE"),
    AUTOSCALING_EC2_INSTANCE_LAUNCH("autoscaling:EC2_INSTANCE_LAUNCH"),
    AUTOSCALING_TEST_NOTIFICATION("autoscaling:TEST_NOTIFICATION");
    
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
