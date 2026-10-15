package com.shinesolutions.aemorchestrator.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@code Details} object of an Auto Scaling event message, holding the instance's {@code Subnet
 * ID} and {@code Availability Zone}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Details {

  private final String subnetId;
  private final String availabilityZone;

  /**
   * Creates the details; also used by Jackson to map the event message JSON.
   *
   * <p>Immutable to fix SpotBugs EI_EXPOSE_REP: Jackson builds it through this constructor instead
   * of setters, using the same JSON property names as before.
   */
  @JsonCreator
  public Details(
      @JsonProperty("Subnet ID") String subnetId,
      @JsonProperty("Availability Zone") String availabilityZone) {
    this.subnetId = subnetId;
    this.availabilityZone = availabilityZone;
  }

  public String getSubnetId() {
    return subnetId;
  }

  public String getAvailabilityZone() {
    return availabilityZone;
  }
}
