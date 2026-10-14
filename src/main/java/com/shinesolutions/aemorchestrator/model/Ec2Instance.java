package com.shinesolutions.aemorchestrator.model;

/**
 * An EC2 instance in an auto scaling group, identified by its instance ID and availability zone.
 */
public class Ec2Instance {

  private String instanceId;
  private String availabilityZone;

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  /** Sets the instance ID and returns this object for chaining. */
  public Ec2Instance withInstanceId(String instanceId) {
    this.setInstanceId(instanceId);
    return this;
  }

  public String getAvailabilityZone() {
    return availabilityZone;
  }

  public void setAvailabilityZone(String availabilityZone) {
    this.availabilityZone = availabilityZone;
  }

  /** Sets the availability zone and returns this object for chaining. */
  public Ec2Instance withAvailabilityZone(String availabilityZone) {
    this.setAvailabilityZone(availabilityZone);
    return this;
  }
}
