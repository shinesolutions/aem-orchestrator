package com.shinesolutions.aemorchestrator.model;

import static org.junit.Assert.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMappingTest {

    @Test
    public void testAemOrchestratorMessageJsonMappingOk() throws Exception {

        File sampleFile = new File(getClass().getResource("/sample-sqs-message-body-1.json").getFile());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(sampleFile);

        EventMessage eventMsg = mapper.readValue(sampleFile, EventMessage.class);

        assertThat(eventMsg.getProgress(), equalTo(root.path("Progress").asInt()));
        assertThat(eventMsg.getAccountId(), equalTo(root.path("AccountId").asText()));
        assertThat(eventMsg.getDescription(), equalTo(root.path("Description").asText()));
        assertThat(eventMsg.getRequestId(), equalTo(root.path("RequestId").asText()));
        assertThat(eventMsg.getEndTime(), equalTo(root.path("EndTime").asText()));
        assertThat(eventMsg.getAutoScalingGroupARN(), equalTo(root.path("AutoScalingGroupARN").asText()));

        assertThat(eventMsg.getActivityId(), equalTo(root.path("ActivityId").asText()));
        assertThat(eventMsg.getStartTime(), equalTo(root.path("StartTime").asText()));
        assertThat(eventMsg.getService(), equalTo(root.path("Service").asText()));

        assertThat(eventMsg.getTime(), equalTo(root.path("Time").asText()));
        assertThat(eventMsg.getEC2InstanceId(), equalTo(root.path("EC2InstanceId").asText()));
        assertThat(eventMsg.getStatusCode(), equalTo(root.path("StatusCode").asText()));

        JsonNode details = root.path("Details");
        assertThat(eventMsg.getDetails().getSubnetID(), equalTo(details.path("Subnet ID").asText()));
        assertThat(eventMsg.getDetails().getAvailabilityZone(), equalTo(details.path("Availability Zone").asText()));

        assertThat(eventMsg.getStatusMessage(), equalTo(root.path("StatusMessage").asText()));
        assertThat(eventMsg.getAutoScalingGroupName(), equalTo(root.path("AutoScalingGroupName").asText()));
        assertThat(eventMsg.getCause(), equalTo(root.path("Cause").asText()));
        assertThat(eventMsg.getEvent(), equalTo(root.path("Event").asText()));
    }

}
