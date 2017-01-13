package com.shinesolutions.aemorchestrator.handler;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.actions.ScaleAction;
import com.shinesolutions.aemorchestrator.model.EventMessage;

@Component
public class AutoscaleLaunchEventHandler implements EventHandler {
    
    @Resource
    private Map<String, ScaleAction> scaleUpAutoScaleGroupMappings;

    public boolean handleEvent(EventMessage message) {
        ScaleAction action = scaleUpAutoScaleGroupMappings.get(message.getAutoScalingGroupName());
        return action.execute(message.getEC2InstanceId());
    }

}
