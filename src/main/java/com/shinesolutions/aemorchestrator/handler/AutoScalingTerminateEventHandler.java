package com.shinesolutions.aemorchestrator.handler;

import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.actions.Action;
import com.shinesolutions.aemorchestrator.model.EventMessage;
import com.shinesolutions.aemorchestrator.util.EventMessageExtractor;

@Component
public class AutoScalingTerminateEventHandler implements MessageHandler {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Resource(name="scaleDownAutoScaleGroupMappings")
    private Map<String, Action> scaleDownAutoScaleGroupMappings;
    
    @Resource
    private EventMessageExtractor eventMessageExtractor;

    public boolean handleEvent(String message) {
        logger.debug("Raw message: " + message);
        boolean success = false;

        try {
            EventMessage eventMessage = eventMessageExtractor.extractMessage(message);
            
            Action action = scaleDownAutoScaleGroupMappings.get(eventMessage.getAutoScalingGroupName());
            if (action == null) {
                logger.warn(
                    "Unable to find 'scale down' action for auto scaling group name: " + eventMessage.getAutoScalingGroupName()
                        + ". Please check the correct auto scaling group names were added to the application config");
            } else {
                success = action.execute(eventMessage.getEC2InstanceId());
            }
        } catch (Exception e) {
            logger.error("Failed to execute 'scale down' action", e);
        }

        return success;
    }

}