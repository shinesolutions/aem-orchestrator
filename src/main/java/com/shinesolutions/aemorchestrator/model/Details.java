package com.shinesolutions.aemorchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Details {
    @JsonProperty("Subnet ID")
    private String subnetID;

    @JsonProperty("Availability Zone")
    private String availabilityZone;

    public String getSubnetID() {
        return subnetID;
    }

    public void setSubnetID(String subnetID) {
        this.subnetID = subnetID;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }
}
