package com.shinesolutions.aemorchestrator.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.util.List;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinesolutions.aemorchestrator.model.AlarmMessage;
import com.shinesolutions.aemorchestrator.model.Dimension;

public class AlarmMessageExtractorTest {
    
    private AlarmMessageExtractor alarmMessageExtractor;

    @Before
    public void setUp() throws Exception {
        alarmMessageExtractor = new AlarmMessageExtractor();
    }

    @Test
    @SuppressWarnings("resource")
    public void testExtractAlarmMessageSuccess() throws Exception {
        File sampleFileMessageOnly = new File(getClass().getResource("/sample-sqs-alarm-message-1.json").getFile());
        String sampleFileContent = new Scanner(sampleFileMessageOnly).useDelimiter("\\Z").next();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(sampleFileMessageOnly);

        AlarmMessage alarmMsg = alarmMessageExtractor.extractMessage(sampleFileContent);

        assertThat(alarmMsg.getAlarmName(), equalTo(root.path("AlarmName").asText()));
        assertThat(alarmMsg.getAlarmDescription(), equalTo(root.path("AlarmDescription").asText()));
        assertThat(alarmMsg.getAwsAccountId(), equalTo(root.path("AWSAccountId").asText()));
        assertThat(alarmMsg.getNewStateValue(), equalTo(root.path("NewStateValue").asText()));
        assertThat(alarmMsg.getNewStateReason(), equalTo(root.path("NewStateReason").asText()));
        assertThat(alarmMsg.getStateChangeTime(), equalTo(root.path("StateChangeTime").asText()));
        assertThat(alarmMsg.getRegion(), equalTo(root.path("Region").asText()));
        assertThat(alarmMsg.getOldStateValue(), equalTo(root.path("OldStateValue").asText()));
        
        JsonNode trigger = root.path("Trigger");
        assertThat(alarmMsg.getTrigger().getMetricName(), equalTo(trigger.path("MetricName").asText()));
        assertThat(alarmMsg.getTrigger().getNamespace(), equalTo(trigger.path("Namespace").asText()));
        assertThat(alarmMsg.getTrigger().getStatisticType(), equalTo(trigger.path("StatisticType").asText()));
        assertThat(alarmMsg.getTrigger().getStatistic(), equalTo(trigger.path("Statistic").asText()));
        assertThat(alarmMsg.getTrigger().getUnit(), equalTo(trigger.path("Unit").asText()));
        assertThat(alarmMsg.getTrigger().getComparisonOperator(), equalTo(trigger.path("ComparisonOperator").asText()));
        assertThat(alarmMsg.getTrigger().getTreatMissingData(), equalTo(trigger.path("TreatMissingData").asText()));
        assertThat(alarmMsg.getTrigger().getEvaluateLowSampleCountPercentile(), 
            equalTo(trigger.path("EvaluateLowSampleCountPercentile").asText()));
        assertThat(alarmMsg.getTrigger().getPeriod(), equalTo(trigger.path("Period").asInt()));
        assertThat(alarmMsg.getTrigger().getEvaluationPeriods(), equalTo(trigger.path("EvaluationPeriods").asInt()));
        assertThat(alarmMsg.getTrigger().getThreshold(), equalTo(trigger.path("Threshold").asDouble()));
        
        JsonNode dimensions = trigger.path("Dimensions");
        List<Dimension> dimensionList = alarmMsg.getTrigger().getDimensions();
        assertThat(dimensionList.size(), equalTo(3));
        
        assertThat(dimensionList.get(0).getName(), equalTo(dimensions.get(0).path("name").asText()));
        assertThat(dimensionList.get(0).getValue(), equalTo(dimensions.get(0).path("value").asText()));
        
        assertThat(dimensionList.get(1).getName(), equalTo(dimensions.get(1).path("name").asText()));
        assertThat(dimensionList.get(1).getValue(), equalTo(dimensions.get(1).path("value").asText()));
        
        assertThat(dimensionList.get(2).getName(), equalTo(dimensions.get(2).path("name").asText()));
        assertThat(dimensionList.get(2).getValue(), equalTo(dimensions.get(2).path("value").asText()));
    }
    
    @Test(expected=JsonParseException.class)
    public void testExtractEventMessageParseFail() throws Exception {
        alarmMessageExtractor.extractMessage("Invalid string");
    }

}
