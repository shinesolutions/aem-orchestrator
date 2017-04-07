package com.shinesolutions.aemorchestrator.model;

public class EC2Instance {
    
    private String instanceId;
    private String availabilityZone;
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
    
    public EC2Instance withInstanceId(String instanceId) {
        this.setInstanceId(instanceId);
        return this;
    }
    
    public String getAvailabilityZone() {
        return availabilityZone;
    }
    
    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }
    
    public EC2Instance withAvailabilityZone(String availabilityZone) {
        this.setAvailabilityZone(availabilityZone);
        return this;
    }
}
