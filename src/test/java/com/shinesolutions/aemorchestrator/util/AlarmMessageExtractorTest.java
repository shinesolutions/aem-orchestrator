package com.shinesolutions.aemorchestrator.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shinesolutions.aemorchestrator.model.AlarmMessage;
import com.shinesolutions.aemorchestrator.model.Dimension;
import java.io.File;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class AlarmMessageExtractorTest {

  private AlarmMessageExtractor alarmMessageExtractor;

  @BeforeEach
  void setUp() throws Exception {
    alarmMessageExtractor = new AlarmMessageExtractor();
  }

  @Test
  @SuppressWarnings("resource")
  void testExtractAlarmMessageSuccess() throws Exception {
    File sampleFileMessageOnly =
        new File(getClass().getResource("/sample-sqs-alarm-message-1.json").getFile());
    String sampleFileContent = new Scanner(sampleFileMessageOnly).useDelimiter("\\Z").next();

    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(sampleFileMessageOnly);

    AlarmMessage alarmMsg = alarmMessageExtractor.extractMessage(sampleFileContent);

    assertThat(alarmMsg.getAlarmName(), equalTo(root.path("AlarmName").asString()));
    assertThat(alarmMsg.getAlarmDescription(), equalTo(root.path("AlarmDescription").asString()));
    assertThat(alarmMsg.getAwsAccountId(), equalTo(root.path("AWSAccountId").asString()));
    assertThat(alarmMsg.getNewStateValue(), equalTo(root.path("NewStateValue").asString()));
    assertThat(alarmMsg.getNewStateReason(), equalTo(root.path("NewStateReason").asString()));
    assertThat(alarmMsg.getStateChangeTime(), equalTo(root.path("StateChangeTime").asString()));
    assertThat(alarmMsg.getRegion(), equalTo(root.path("Region").asString()));
    assertThat(alarmMsg.getOldStateValue(), equalTo(root.path("OldStateValue").asString()));

    JsonNode trigger = root.path("Trigger");
    assertThat(
        alarmMsg.getTrigger().getMetricName(), equalTo(trigger.path("MetricName").asString()));
    assertThat(alarmMsg.getTrigger().getNamespace(), equalTo(trigger.path("Namespace").asString()));
    assertThat(
        alarmMsg.getTrigger().getStatisticType(),
        equalTo(trigger.path("StatisticType").asString()));
    assertThat(alarmMsg.getTrigger().getStatistic(), equalTo(trigger.path("Statistic").asString()));
    assertThat(alarmMsg.getTrigger().getUnit(), equalTo(trigger.path("Unit").asString()));
    assertThat(
        alarmMsg.getTrigger().getComparisonOperator(),
        equalTo(trigger.path("ComparisonOperator").asString()));
    assertThat(
        alarmMsg.getTrigger().getTreatMissingData(),
        equalTo(trigger.path("TreatMissingData").asString()));
    assertThat(
        alarmMsg.getTrigger().getEvaluateLowSampleCountPercentile(),
        equalTo(trigger.path("EvaluateLowSampleCountPercentile").asString()));
    assertThat(alarmMsg.getTrigger().getPeriod(), equalTo(trigger.path("Period").asInt()));
    assertThat(
        alarmMsg.getTrigger().getEvaluationPeriods(),
        equalTo(trigger.path("EvaluationPeriods").asInt()));
    assertThat(alarmMsg.getTrigger().getThreshold(), equalTo(trigger.path("Threshold").asDouble()));

    JsonNode dimensions = trigger.path("Dimensions");
    List<Dimension> dimensionList = alarmMsg.getTrigger().getDimensions();
    assertThat(dimensionList.size(), equalTo(3));

    assertThat(dimensionList.get(0).getName(), equalTo(dimensions.get(0).path("name").asString()));
    assertThat(
        dimensionList.get(0).getValue(), equalTo(dimensions.get(0).path("value").asString()));

    assertThat(dimensionList.get(1).getName(), equalTo(dimensions.get(1).path("name").asString()));
    assertThat(
        dimensionList.get(1).getValue(), equalTo(dimensions.get(1).path("value").asString()));

    assertThat(dimensionList.get(2).getName(), equalTo(dimensions.get(2).path("name").asString()));
    assertThat(
        dimensionList.get(2).getValue(), equalTo(dimensions.get(2).path("value").asString()));
  }

  @Test
  void testExtractEventMessageParseFail() throws Exception {
    assertThrows(
        StreamReadException.class,
        () -> {
          alarmMessageExtractor.extractMessage("Invalid string");
        });
  }
}
