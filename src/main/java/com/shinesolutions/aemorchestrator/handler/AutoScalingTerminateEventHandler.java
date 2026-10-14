package com.shinesolutions.aemorchestrator.handler;

import com.shinesolutions.aemorchestrator.actions.Action;
import com.shinesolutions.aemorchestrator.model.EventMessage;
import com.shinesolutions.aemorchestrator.util.EventMessageExtractor;
import jakarta.annotation.Resource;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles Auto Scaling termination notifications by running the scale-down {@link Action} mapped to
 * the event's Auto Scaling group.
 */
@Component
public class AutoScalingTerminateEventHandler implements MessageHandler {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource(name = "scaleDownAutoScaleGroupMappings")
  private Map<String, Action> scaleDownAutoScaleGroupMappings;

  @Resource private EventMessageExtractor eventMessageExtractor;

  /**
   * Runs the scale-down action for the terminated instance's Auto Scaling group.
   *
   * @param message the Auto Scaling termination event JSON
   * @return the action's result; {@code false} if no action is mapped to the Auto Scaling group or
   *     processing fails, leaving the message on the queue
   */
  public boolean handleEvent(String message) {
    logger.debug("Raw message: " + message);
    boolean success = false;

    try {
      EventMessage eventMessage = eventMessageExtractor.extractMessage(message);

      Action action = scaleDownAutoScaleGroupMappings.get(eventMessage.getAutoScalingGroupName());
      if (action == null) {
        logger.warn(
            "Unable to find 'scale down' action for auto scaling group name: "
                + eventMessage.getAutoScalingGroupName()
                + ". Please check the correct auto scaling group names were added to the"
                + " application config");
      } else {
        success = action.execute(eventMessage.getEc2InstanceId());
      }
    } catch (Exception e) {
      logger.error("Failed to execute 'scale down' action", e);
    }

    return success;
  }
}
