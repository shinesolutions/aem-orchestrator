package com.shinesolutions.aemorchestrator.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.util.Scanner;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinesolutions.aemorchestrator.model.EventMessage;

public class MessageExtractorTest {

    @Test
    @SuppressWarnings("resource")
    public void testExtractEventMessageSuccess() throws Exception {

        File sampleFileMessageOnly = new File(getClass().getResource("/sample-sqs-message-body-1.json").getFile());
        File sampleFileFull = new File(getClass().getResource("/sample-sqs-message-body-2.json").getFile());
        String sampleFileContent = new Scanner(sampleFileFull).useDelimiter("\\Z").next();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(sampleFileMessageOnly);

        EventMessage eventMsg = MessageExtractor.extractEventMessage(sampleFileContent);

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
    
    @Test(expected=JsonParseException.class)
    public void testExtractEventMessageParseFail() throws Exception {
        MessageExtractor.extractEventMessage("Invalid string");
    }
}
