package com.shinesolutions.aemorchestrator.handler;

import com.shinesolutions.aemorchestrator.actions.Action;
import com.shinesolutions.aemorchestrator.model.EventMessage;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import com.shinesolutions.aemorchestrator.util.EventMessageExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class AutoScalingLaunchEventHandler implements MessageHandler {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource(name="scaleUpAutoScaleGroupMappings")
    private Map<String, Action> scaleUpAutoScaleGroupMappings;
    
    @Resource
    private AwsHelperService awsHelperService;
    
    @Resource
    private EventMessageExtractor eventMessageExtractor;

    @Override
    public boolean handleEvent(String message) {
        logger.debug("Raw message: " + message);
        boolean success = false;

        try {
            EventMessage eventMessage = eventMessageExtractor.extractMessage(message);

            if (eventMessage.getDescription() != null && eventMessage.getDescription().startsWith("Moving EC2 instance out of Standby")) {
                logger.info("Detected a 'Moving EC2 instance out of Standby' event. The stack should already be aware of this instance. No action to perform.");
                return true;
            }

            Action action = scaleUpAutoScaleGroupMappings.get(eventMessage.getAutoScalingGroupName());
            if (action == null) {
                logger.warn(
                    "Unable to find 'scale up' action for auto scaling group name: " + eventMessage.getAutoScalingGroupName()
                        + ". Please check the correct auto scaling group names were added to the application config");
            } else {
                if(awsHelperService.isInstanceRunning(eventMessage.getEC2InstanceId())) {
                    success = action.execute(eventMessage.getEC2InstanceId());
                } else {
                    logger.warn("Instance " + eventMessage.getEC2InstanceId() + 
                        " does not appear to be running. Ignoring message (no action taken)");
                    success = true;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to execute 'scale up' action", e);
        }

        return success;
    }
}
