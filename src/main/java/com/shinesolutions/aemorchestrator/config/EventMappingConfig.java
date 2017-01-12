package com.shinesolutions.aemorchestrator.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shinesolutions.aemorchestrator.handler.EventHandler;
import com.shinesolutions.aemorchestrator.handler.AutoscaleTerminateEventHandler;
import com.shinesolutions.aemorchestrator.handler.AutoscaleLaunchEventHandler;

@Configuration
public class EventMappingConfig {

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

}
