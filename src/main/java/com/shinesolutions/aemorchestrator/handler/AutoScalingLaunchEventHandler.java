package com.shinesolutions.aemorchestrator.handler;

import com.shinesolutions.aemorchestrator.actions.Action;
import com.shinesolutions.aemorchestrator.model.EventMessage;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import com.shinesolutions.aemorchestrator.util.EventMessageExtractor;
import jakarta.annotation.Resource;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles Auto Scaling launch notifications by running the scale-up {@link Action} mapped to the
 * event's Auto Scaling group.
 *
 * <p>Returns {@code true} (removing the message from the queue) without running an action when the
 * event is an instance moving out of Standby or the launched instance is no longer running.
 * Otherwise returns the action's result, or {@code false} if no action is mapped to the group or
 * processing fails.
 */
@Component
public class AutoScalingLaunchEventHandler implements MessageHandler {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource(name = "scaleUpAutoScaleGroupMappings")
  private Map<String, Action> scaleUpAutoScaleGroupMappings;

  @Resource private AwsHelperService awsHelperService;

  @Resource private EventMessageExtractor eventMessageExtractor;

  @Override
  public boolean handleEvent(String message) {
    logger.debug("Raw message: " + message);
    boolean success = false;

    try {
      EventMessage eventMessage = eventMessageExtractor.extractMessage(message);

      if (eventMessage.getDescription() != null
          && eventMessage.getDescription().startsWith("Moving EC2 instance out of Standby")) {
        logger.info(
            "Detected a 'Moving EC2 instance out of Standby' event. The stack should already be"
                + " aware of this instance. No action to perform.");
        return true;
      }

      Action action = scaleUpAutoScaleGroupMappings.get(eventMessage.getAutoScalingGroupName());
      if (action == null) {
        logger.warn(
            "Unable to find 'scale up' action for auto scaling group name: "
                + eventMessage.getAutoScalingGroupName()
                + ". Please check the correct auto scaling group names were added to the"
                + " application config");
      } else {
        if (awsHelperService.isInstanceRunning(eventMessage.getEc2InstanceId())) {
          success = action.execute(eventMessage.getEc2InstanceId());
        } else {
          logger.warn(
              "Instance "
                  + eventMessage.getEc2InstanceId()
                  + " does not appear to be running. Ignoring message (no action taken)");
          success = true;
        }
      }
    } catch (Exception e) {
      logger.error("Failed to execute 'scale up' action", e);
    }

    return success;
  }
}
