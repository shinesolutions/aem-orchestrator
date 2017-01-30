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
import com.shinesolutions.aemorchestrator.model.AutoScaleGroupNames;

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
        final AutoScaleGroupNames asgNames) {

        Map<String, ScaleAction> mappings = new HashMap<String, ScaleAction>();
        
        mappings.put(asgNames.getPublishDispatcher(), scaleDownPublisherDispatcherAction);
        mappings.put(asgNames.getPublish(), scaleDownPublisherAction);
        mappings.put(asgNames.getAuthorDispatcher(), scaleDownAuthorDispatcherAction);

        return mappings;
    }
    
    @Bean
    public Map<String, ScaleAction> scaleUpAutoScaleGroupMappings(
        final ScaleUpPublisherDispatcherAction scaleUpPublisherDispatcherAction,
        final ScaleUpPublisherAction scaleUpPublisherAction,
        final ScaleUpAuthorDispatcherAction scaleUpAuthorDispatcherAction,
        final AutoScaleGroupNames asgNames) {

        Map<String, ScaleAction> mappings = new HashMap<String, ScaleAction>();
   
        mappings.put(asgNames.getPublishDispatcher(), scaleUpPublisherDispatcherAction);
        mappings.put(asgNames.getPublish(), scaleUpPublisherAction);
        mappings.put(asgNames.getAuthorDispatcher(), scaleUpAuthorDispatcherAction);

        return mappings;
    }

}
