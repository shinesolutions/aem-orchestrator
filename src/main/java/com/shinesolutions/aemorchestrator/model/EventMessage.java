package com.shinesolutions.aemorchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Represents the message but for a standard AEM Orchestrator message 
 */
public class EventMessage {

    @JsonProperty("Progress")
    private Integer progress;

    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("RequestId")
    private String requestId;

    @JsonProperty("StartTime")
    private String startTime;

    @JsonProperty("EndTime")
    private String endTime;

    @JsonProperty("AutoScalingGroupARN")
    private String autoScalingGroupARN;

    @JsonProperty("AutoScalingGroupName")
    private String autoScalingGroupName;

    @JsonProperty("ActivityId")
    private String activityId;

    @JsonProperty("Service")
    private String service;

    @JsonProperty("Time")
    private String time;

    @JsonProperty("EC2InstanceId")
    private String eC2InstanceId;

    @JsonProperty("StatusCode")
    private String statusCode;

    @JsonProperty("StatusMessage")
    private String statusMessage;

    @JsonProperty("Details")
    private Details details;

    @JsonProperty("Cause")
    private String cause;

    @JsonProperty("Event")
    private String event;

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getAutoScalingGroupARN() {
        return autoScalingGroupARN;
    }

    public void setAutoScalingGroupARN(String autoScalingGroupARN) {
        this.autoScalingGroupARN = autoScalingGroupARN;
    }

    public String getAutoScalingGroupName() {
        return autoScalingGroupName;
    }

    public void setAutoScalingGroupName(String autoScalingGroupName) {
        this.autoScalingGroupName = autoScalingGroupName;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String geteC2InstanceId() {
        return eC2InstanceId;
    }

    public void seteC2InstanceId(String eC2InstanceId) {
        this.eC2InstanceId = eC2InstanceId;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Details getDetails() {
        return details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
