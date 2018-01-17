package com.shinesolutions.aemorchestrator.config;

import com.shinesolutions.aemorchestrator.actions.*;
import com.shinesolutions.aemorchestrator.handler.*;
import com.shinesolutions.aemorchestrator.model.EnvironmentValues;
import com.shinesolutions.aemorchestrator.model.EventType;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MappingConfigTest {
    
    private MappingConfig mappingConfig;
    
    @Before
    public void setup() {
        mappingConfig = new MappingConfig();
    }
    
    @Test
    public void testEventTypeHandlerMappings() {
        AutoScalingTerminateEventHandler scaleDownEventHandler = mock(AutoScalingTerminateEventHandler.class);
        AutoScalingLaunchEventHandler scaleUpEventHandler = mock(AutoScalingLaunchEventHandler.class);
        TestNotificationEventHandler testNotificationEventHandler = mock(TestNotificationEventHandler.class);
        AlarmMessageHandler alarmMessageHandler = mock(AlarmMessageHandler.class);
        
        Map<String, MessageHandler> mappings = mappingConfig.eventTypeHandlerMappings(
                scaleDownEventHandler,
                scaleUpEventHandler,
                testNotificationEventHandler,
                alarmMessageHandler);
        
        assertThat(mappings.get(EventType.AUTOSCALING_EC2_INSTANCE_TERMINATE.getValue()), equalTo(scaleDownEventHandler));
        assertThat(mappings.get(EventType.AUTOSCALING_EC2_INSTANCE_LAUNCH.getValue()), equalTo(scaleUpEventHandler));
        assertThat(mappings.get(EventType.AUTOSCALING_TEST_NOTIFICATION.getValue()), equalTo(testNotificationEventHandler));
        assertThat(mappings.get(EventType.ALARM.getValue()), equalTo(alarmMessageHandler));
    }
    
    @Test
    public void testScaleDownAutoScaleGroupMappings() {
        ScaleDownPublishDispatcherAction scaleDownPublishDispatcherAction = mock(ScaleDownPublishDispatcherAction.class);
        ScaleDownPublishAction scaleDownPublishAction = mock(ScaleDownPublishAction.class);
        ScaleDownAuthorDispatcherAction scaleDownAuthorDispatcherAction = mock(ScaleDownAuthorDispatcherAction.class);
        
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        EnvironmentValues environmentValues = mock(EnvironmentValues.class);
        when(environmentValues.getAutoScaleGroupNameForPublishDispatcher()).thenReturn(key1);
        when(environmentValues.getAutoScaleGroupNameForPublish()).thenReturn(key2);
        when(environmentValues.getAutoScaleGroupNameForAuthorDispatcher()).thenReturn(key3);
        
        Map<String, Action> mappings = mappingConfig.scaleDownAutoScaleGroupMappings(
                scaleDownPublishDispatcherAction,
                scaleDownPublishAction,
                scaleDownAuthorDispatcherAction,
                environmentValues);
        
        assertThat(mappings.get(key1), equalTo(scaleDownPublishDispatcherAction));
        assertThat(mappings.get(key2), equalTo(scaleDownPublishAction));
        assertThat(mappings.get(key3), equalTo(scaleDownAuthorDispatcherAction));
    }
    
    @Test
    public void testScaleUpAutoScaleGroupMappings() {
        ScaleUpPublishDispatcherAction scaleUpPublishDispatcherAction = mock(ScaleUpPublishDispatcherAction.class);
        ScaleUpPublishAction scaleUpPublishAction = mock(ScaleUpPublishAction.class);
        ScaleUpAuthorDispatcherAction scaleUpAuthorDispatcherAction = mock(ScaleUpAuthorDispatcherAction.class);
        
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        EnvironmentValues environmentValues = mock(EnvironmentValues.class);
        when(environmentValues.getAutoScaleGroupNameForPublishDispatcher()).thenReturn(key1);
        when(environmentValues.getAutoScaleGroupNameForPublish()).thenReturn(key2);
        when(environmentValues.getAutoScaleGroupNameForAuthorDispatcher()).thenReturn(key3);
        
        Map<String, Action> mappings = mappingConfig.scaleUpAutoScaleGroupMappings(
                scaleUpPublishDispatcherAction,
                scaleUpPublishAction,
                scaleUpAuthorDispatcherAction,
                environmentValues);
        
        assertThat(mappings.get(key1), equalTo(scaleUpPublishDispatcherAction));
        assertThat(mappings.get(key2), equalTo(scaleUpPublishAction));
        assertThat(mappings.get(key3), equalTo(scaleUpAuthorDispatcherAction));
    }
}
