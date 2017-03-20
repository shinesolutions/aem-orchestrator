package com.shinesolutions.aemorchestrator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlarmMessage {

    @JsonProperty("AlarmName")
    private String alarmName;

    @JsonProperty("AlarmDescription")
    private String alarmDescription;

    @JsonProperty("AWSAccountId")
    private String awsAccountId;

    @JsonProperty("NewStateValue")
    private String newStateValue;

    @JsonProperty("NewStateReason")
    private String newStateReason;

    @JsonProperty("StateChangeTime")
    private String stateChangeTime;

    @JsonProperty("Region")
    private String region;

    @JsonProperty("OldStateValue")
    private String oldStateValue;

    @JsonProperty("Trigger")
    private Trigger trigger;

    public String getAlarmName() {
        return alarmName;
    }

    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    public String getAlarmDescription() {
        return alarmDescription;
    }

    public void setAlarmDescription(String alarmDescription) {
        this.alarmDescription = alarmDescription;
    }

    public String getAwsAccountId() {
        return awsAccountId;
    }

    public void setAwsAccountId(String awsAccountId) {
        this.awsAccountId = awsAccountId;
    }

    public String getNewStateValue() {
        return newStateValue;
    }

    public void setNewStateValue(String newStateValue) {
        this.newStateValue = newStateValue;
    }

    public String getNewStateReason() {
        return newStateReason;
    }

    public void setNewStateReason(String newStateReason) {
        this.newStateReason = newStateReason;
    }

    public String getStateChangeTime() {
        return stateChangeTime;
    }

    public void setStateChangeTime(String stateChangeTime) {
        this.stateChangeTime = stateChangeTime;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getOldStateValue() {
        return oldStateValue;
    }

    public void setOldStateValue(String oldStateValue) {
        this.oldStateValue = oldStateValue;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

}
