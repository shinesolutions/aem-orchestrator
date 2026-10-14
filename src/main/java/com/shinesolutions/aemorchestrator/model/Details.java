package com.shinesolutions.aemorchestrator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@code Details} object of an Auto Scaling event message, holding the instance's {@code Subnet
 * ID} and {@code Availability Zone}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Details {

  @JsonProperty("Subnet ID")
  private String subnetId;

  @JsonProperty("Availability Zone")
  private String availabilityZone;

  public String getSubnetId() {
    return subnetId;
  }

  public void setSubnetId(String subnetId) {
    this.subnetId = subnetId;
  }

  public String getAvailabilityZone() {
    return availabilityZone;
  }

  public void setAvailabilityZone(String availabilityZone) {
    this.availabilityZone = availabilityZone;
  }
}
