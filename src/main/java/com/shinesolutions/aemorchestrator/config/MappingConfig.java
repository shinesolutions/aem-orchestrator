package com.shinesolutions.aemorchestrator.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shinesolutions.aemorchestrator.actions.ScaleAction;
import com.shinesolutions.aemorchestrator.actions.ScaleDownAuthorDispatcherAction;
import com.shinesolutions.aemorchestrator.actions.ScaleDownPublisherAction;
import com.shinesolutions.aemorchestrator.actions.ScaleDownPublisherDispatcherAction;
import com.shinesolutions.aemorchestrator.actions.ScaleUpAuthorDispatcherAction;
import com.shinesolutions.aemorchestrator.actions.ScaleUpPublisherAction;
import com.shinesolutions.aemorchestrator.actions.ScaleUpPublisherDispatcherAction;
import com.shinesolutions.aemorchestrator.handler.AutoscaleLaunchEventHandler;
import com.shinesolutions.aemorchestrator.handler.AutoscaleTerminateEventHandler;
import com.shinesolutions.aemorchestrator.handler.EventHandler;
import com.shinesolutions.aemorchestrator.model.EnvironmentValues;

@Configuration
public class MappingConfig {

    @Bean
    public Map<String, EventHandler> eventTypeHandlerMappings(
        final AutoscaleTerminateEventHandler autoscaleTerminateEventHandler,
        final AutoscaleLaunchEventHandler autoscaleLaunchEventHandler) {

        Map<String, EventHandler> mappings = new HashMap<String, EventHandler>();
        mappings.put("autoscaling:EC2_INSTANCE_TERMINATE", autoscaleTerminateEventHandler);
        mappings.put("autoscaling:EC2_INSTANCE_LAUNCH", autoscaleLaunchEventHandler);

        return mappings;
    }

    @Bean
    public Map<String, ScaleAction> scaleDownAutoScaleGroupMappings(
        final ScaleDownPublisherDispatcherAction scaleDownPublisherDispatcherAction,
        final ScaleDownPublisherAction scaleDownPublisherAction,
        final ScaleDownAuthorDispatcherAction scaleDownAuthorDispatcherAction,
        final EnvironmentValues envValues) {

        Map<String, ScaleAction> mappings = new HashMap<String, ScaleAction>();
        
        mappings.put(envValues.getAutoScaleGroupNameForPublishDispatcher(), scaleDownPublisherDispatcherAction);
        mappings.put(envValues.getAutoScaleGroupNameForPublish(), scaleDownPublisherAction);
        mappings.put(envValues.getAutoScaleGroupNameForAuthorDispatcher(), scaleDownAuthorDispatcherAction);

        return mappings;
    }
    
    @Bean
    public Map<String, ScaleAction> scaleUpAutoScaleGroupMappings(
        final ScaleUpPublisherDispatcherAction scaleUpPublisherDispatcherAction,
        final ScaleUpPublisherAction scaleUpPublisherAction,
        final ScaleUpAuthorDispatcherAction scaleUpAuthorDispatcherAction,
        final EnvironmentValues envValues) {

        Map<String, ScaleAction> mappings = new HashMap<String, ScaleAction>();
   
        mappings.put(envValues.getAutoScaleGroupNameForPublishDispatcher(), scaleUpPublisherDispatcherAction);
        mappings.put(envValues.getAutoScaleGroupNameForPublish(), scaleUpPublisherAction);
        mappings.put(envValues.getAutoScaleGroupNameForAuthorDispatcher(), scaleUpAuthorDispatcherAction);

        return mappings;
    }

}
