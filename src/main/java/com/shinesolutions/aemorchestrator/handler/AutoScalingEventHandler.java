package com.shinesolutions.aemorchestrator.handler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinesolutions.aemorchestrator.actions.ScaleAction;
import com.shinesolutions.aemorchestrator.model.EventMessage;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;

public class AutoScalingEventHandler implements EventHandler {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, ScaleAction> autoScaleActionGroupMappings;
    
    private AwsHelperService awsHelperService;

    public AutoScalingEventHandler(Map<String, ScaleAction> autoScaleActionGroupMappings,
        AwsHelperService awsHelperService) {
        super();
        this.autoScaleActionGroupMappings = autoScaleActionGroupMappings;
        this.awsHelperService = awsHelperService;
    }

    @Override
    public boolean handleEvent(EventMessage message) {
        boolean success = false;
        try {
            ScaleAction action = autoScaleActionGroupMappings.get(message.getAutoScalingGroupName());
            if (action == null) {
                logger.warn(
                    "Unable to find action for auto scaling group name: " + message.getAutoScalingGroupName()
                        + ". Please check the correct auto scaling group names were added to the application config");
            } else {
                if(awsHelperService.isInstanceRunning(message.getEC2InstanceId())) {
                    success = action.execute(message.getEC2InstanceId());
                } else {
                    logger.warn("Instance " + message.getEC2InstanceId() + 
                        " does not appear to be running. Ignoring message (no action taken)");
                    success = true;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to execute " + message.getEvent() + " action for auto scaling group name: " + 
                message.getAutoScalingGroupName(), e);
        }

        return success;
    }
}
