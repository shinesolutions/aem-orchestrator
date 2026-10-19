package com.shinesolutions.aemorchestrator.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shinesolutions.aemorchestrator.model.EventMessage;
import java.io.File;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class EventMessageExtractorTest {

  private EventMessageExtractor eventMessageExtractor;

  @BeforeEach
  void setUp() throws Exception {
    eventMessageExtractor = new EventMessageExtractor();
  }

  @Test
  @SuppressWarnings("resource")
  void testExtractEventMessageSuccess() throws Exception {

    File sampleFileMessageOnly =
        new File(getClass().getResource("/sample-sqs-event-message-1.json").getFile());
    String sampleFileContent = new Scanner(sampleFileMessageOnly).useDelimiter("\\Z").next();

    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(sampleFileMessageOnly);

    EventMessage eventMsg = eventMessageExtractor.extractMessage(sampleFileContent);

    assertThat(eventMsg.getProgress(), equalTo(root.path("Progress").asInt()));
    assertThat(eventMsg.getAccountId(), equalTo(root.path("AccountId").asString()));
    assertThat(eventMsg.getDescription(), equalTo(root.path("Description").asString()));
    assertThat(eventMsg.getRequestId(), equalTo(root.path("RequestId").asString()));
    assertThat(eventMsg.getEndTime(), equalTo(root.path("EndTime").asString()));
    assertThat(
        eventMsg.getAutoScalingGroupArn(), equalTo(root.path("AutoScalingGroupARN").asString()));

    assertThat(eventMsg.getActivityId(), equalTo(root.path("ActivityId").asString()));
    assertThat(eventMsg.getStartTime(), equalTo(root.path("StartTime").asString()));
    assertThat(eventMsg.getService(), equalTo(root.path("Service").asString()));

    assertThat(eventMsg.getTime(), equalTo(root.path("Time").asString()));
    assertThat(eventMsg.getEc2InstanceId(), equalTo(root.path("EC2InstanceId").asString()));
    assertThat(eventMsg.getStatusCode(), equalTo(root.path("StatusCode").asString()));

    JsonNode details = root.path("Details");
    assertThat(eventMsg.getDetails().getSubnetId(), equalTo(details.path("Subnet ID").asString()));
    assertThat(
        eventMsg.getDetails().getAvailabilityZone(),
        equalTo(details.path("Availability Zone").asString()));

    assertThat(eventMsg.getStatusMessage(), equalTo(root.path("StatusMessage").asString()));
    assertThat(
        eventMsg.getAutoScalingGroupName(), equalTo(root.path("AutoScalingGroupName").asString()));
    assertThat(eventMsg.getCause(), equalTo(root.path("Cause").asString()));
    assertThat(eventMsg.getEvent(), equalTo(root.path("Event").asString()));
  }

  @Test
  void testExtractEventMessageParseFail() throws Exception {
    assertThrows(
        StreamReadException.class,
        () -> {
          eventMessageExtractor.extractMessage("Invalid string");
        });
  }
}
