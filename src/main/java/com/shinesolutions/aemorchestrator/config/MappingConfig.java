package com.shinesolutions.aemorchestrator.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shinesolutions.aemorchestrator.actions.Action;
import com.shinesolutions.aemorchestrator.actions.ScaleDownAuthorDispatcherAction;
import com.shinesolutions.aemorchestrator.actions.ScaleDownPublishAction;
import com.shinesolutions.aemorchestrator.actions.ScaleDownPublishDispatcherAction;
import com.shinesolutions.aemorchestrator.actions.ScaleDownPreviewPublishAction;
import com.shinesolutions.aemorchestrator.actions.ScaleDownPreviewPublishDispatcherAction;
import com.shinesolutions.aemorchestrator.actions.ScaleUpAuthorDispatcherAction;
import com.shinesolutions.aemorchestrator.actions.ScaleUpPublishAction;
import com.shinesolutions.aemorchestrator.actions.ScaleUpPublishDispatcherAction;
import com.shinesolutions.aemorchestrator.actions.ScaleUpPreviewPublishAction;
import com.shinesolutions.aemorchestrator.actions.ScaleUpPreviewPublishDispatcherAction;
import com.shinesolutions.aemorchestrator.handler.AlarmMessageHandler;
import com.shinesolutions.aemorchestrator.handler.AutoScalingLaunchEventHandler;
import com.shinesolutions.aemorchestrator.handler.AutoScalingTerminateEventHandler;
import com.shinesolutions.aemorchestrator.handler.MessageHandler;
import com.shinesolutions.aemorchestrator.handler.TestNotificationEventHandler;
import com.shinesolutions.aemorchestrator.model.EnvironmentValues;
import com.shinesolutions.aemorchestrator.model.EventType;

@Configuration
public class MappingConfig {

    @Bean
    public Map<String, MessageHandler> eventTypeHandlerMappings(
        final AutoScalingTerminateEventHandler scaleDownEventHandler,
        final AutoScalingLaunchEventHandler scaleUpEventHandler,
        final TestNotificationEventHandler testNotificationEventHandler,
        final AlarmMessageHandler alarmMessageHandler) {

        Map<String, MessageHandler> mappings = new HashMap<String, MessageHandler>();
        mappings.put(EventType.AUTOSCALING_EC2_INSTANCE_TERMINATE.getValue(), scaleDownEventHandler);
        mappings.put(EventType.AUTOSCALING_EC2_INSTANCE_LAUNCH.getValue(), scaleUpEventHandler);
        mappings.put(EventType.AUTOSCALING_TEST_NOTIFICATION.getValue(), testNotificationEventHandler);
        mappings.put(EventType.ALARM.getValue(), alarmMessageHandler);

        return mappings;
    }

    @Bean(name="scaleDownAutoScaleGroupMappings")
    public Map<String, Action> scaleDownAutoScaleGroupMappings(
        final ScaleDownPublishDispatcherAction scaleDownPublishDispatcherAction,
        final ScaleDownPublishAction scaleDownPublishAction,
        final ScaleDownPreviewPublishDispatcherAction scaleDownPreviewPublishDispatcherAction,
        final ScaleDownPreviewPublishAction scaleDownPreviewPublishAction,
        final ScaleDownAuthorDispatcherAction scaleDownAuthorDispatcherAction,
        final EnvironmentValues envValues) {

        Map<String, Action> mappings = new HashMap<String, Action>();

        mappings.put(envValues.getAutoScaleGroupNameForPublishDispatcher(), scaleDownPublishDispatcherAction);
        mappings.put(envValues.getAutoScaleGroupNameForPublish(), scaleDownPublishAction);
        mappings.put(envValues.getAutoScaleGroupNameForPreviewPublishDispatcher(), scaleDownPreviewPublishDispatcherAction);
        mappings.put(envValues.getAutoScaleGroupNameForPreviewPublish(), scaleDownPreviewPublishAction);
        mappings.put(envValues.getAutoScaleGroupNameForAuthorDispatcher(), scaleDownAuthorDispatcherAction);

        return mappings;
    }

    @Bean(name="scaleUpAutoScaleGroupMappings")
    public Map<String, Action> scaleUpAutoScaleGroupMappings(
        final ScaleUpPublishDispatcherAction scaleUpPublishDispatcherAction,
        final ScaleUpPublishAction scaleUpPublishAction,
        final ScaleUpPreviewPublishDispatcherAction scaleUpPreviewPublishDispatcherAction,
        final ScaleUpPreviewPublishAction scaleUpPreviewPublishAction,
        final ScaleUpAuthorDispatcherAction scaleUpAuthorDispatcherAction,
        final EnvironmentValues envValues) {

        Map<String, Action> mappings = new HashMap<String, Action>();

        mappings.put(envValues.getAutoScaleGroupNameForPublishDispatcher(), scaleUpPublishDispatcherAction);
        mappings.put(envValues.getAutoScaleGroupNameForPublish(), scaleUpPublishAction);
        mappings.put(envValues.getAutoScaleGroupNameForPreviewPublishDispatcher(), scaleUpPreviewPublishDispatcherAction);
        mappings.put(envValues.getAutoScaleGroupNameForPreviewPublish(), scaleUpPreviewPublishAction);
        mappings.put(envValues.getAutoScaleGroupNameForAuthorDispatcher(), scaleUpAuthorDispatcherAction);

        return mappings;
    }
}
