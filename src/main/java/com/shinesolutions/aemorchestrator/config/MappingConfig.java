package com.shinesolutions.aemorchestrator.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shinesolutions.aemorchestrator.handler.EventHandler;
import com.shinesolutions.aemorchestrator.handler.AutoscaleTerminateEventHandler;
import com.shinesolutions.aemorchestrator.actions.ScaleAction;
import com.shinesolutions.aemorchestrator.actions.ScaleDownAuthorDispatcherAction;
import com.shinesolutions.aemorchestrator.actions.ScaleDownPublisherAction;
import com.shinesolutions.aemorchestrator.actions.ScaleDownPublisherDispatcherAction;
import com.shinesolutions.aemorchestrator.actions.ScaleUpAuthorDispatcherAction;
import com.shinesolutions.aemorchestrator.actions.ScaleUpPublisherAction;
import com.shinesolutions.aemorchestrator.actions.ScaleUpPublisherDispatcherAction;
import com.shinesolutions.aemorchestrator.handler.AutoscaleLaunchEventHandler;

@Configuration
public class MappingConfig {

    @Value("${aws.autoscale.group.name.publisherDispatcher}")
    private String publisherDispatcherGroupName;

    @Value("${aws.autoscale.group.name.publisher}")
    private String publisherGroupName;

    @Value("${aws.autoscale.group.name.authorDispatcher}")
    private String authorDispatcherGroupName;

    @Bean
    @SuppressWarnings("serial")
    public Map<String, EventHandler> eventTypeHandlerMappings(
        final AutoscaleTerminateEventHandler autoscaleTerminateEventHandler,
        final AutoscaleLaunchEventHandler autoscaleLaunchEventHandler) {

        Map<String, EventHandler> mappings = new HashMap<String, EventHandler>() {
            {
                put("autoscaling:EC2_INSTANCE_TERMINATE", autoscaleTerminateEventHandler);
                put("autoscaling:EC2_INSTANCE_LAUNCH", autoscaleLaunchEventHandler);
            }
        };

        return mappings;
    }

    @Bean
    @SuppressWarnings("serial")
    public Map<String, ScaleAction> scaleDownAutoScaleGroupMappings(
        final ScaleDownPublisherDispatcherAction scaleDownPublisherDispatcherAction,
        final ScaleDownPublisherAction scaleDownPublisherAction,
        final ScaleDownAuthorDispatcherAction scaleDownAuthorDispatcherAction) {

        Map<String, ScaleAction> mappings = new HashMap<String, ScaleAction>() {
            {
                put(publisherDispatcherGroupName, scaleDownPublisherDispatcherAction);
                put(publisherGroupName, scaleDownPublisherAction);
                put(authorDispatcherGroupName, scaleDownAuthorDispatcherAction);
            }
        };

        return mappings;
    }
    
    @Bean
    @SuppressWarnings("serial")
    public Map<String, ScaleAction> scaleUpAutoScaleGroupMappings(
        final ScaleUpPublisherDispatcherAction scaleUpPublisherDispatcherAction,
        final ScaleUpPublisherAction scaleUpPublisherAction,
        final ScaleUpAuthorDispatcherAction scaleUpAuthorDispatcherAction) {

        Map<String, ScaleAction> mappings = new HashMap<String, ScaleAction>() {
            {
                put(publisherDispatcherGroupName, scaleUpPublisherDispatcherAction);
                put(publisherGroupName, scaleUpPublisherAction);
                put(authorDispatcherGroupName, scaleUpAuthorDispatcherAction);
            }
        };

        return mappings;
    }

}
