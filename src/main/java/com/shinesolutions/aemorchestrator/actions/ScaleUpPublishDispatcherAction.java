package com.shinesolutions.aemorchestrator.actions;

import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Scale-up action run when a publish dispatcher instance is launched. */
@Component
public class ScaleUpPublishDispatcherAction implements Action {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource private AemInstanceHelperService aemHelperService;

  /**
   * Sets the publish Auto Scaling group's desired capacity to match the publish dispatcher group's.
   *
   * <p>No change is made if the two desired capacities are already equal. The publish instance
   * launched as a result is paired with an unpaired dispatcher by {@link ScaleUpPublishAction}.
   *
   * @param instanceId the EC2 instance ID of the launched publish dispatcher (not used)
   * @return always {@code true}, so the SQS message is removed from the queue
   */
  public boolean execute(String instanceId) {
    logger.info("ScaleUpPublishDispatcherAction executing");

    // Change publish auto scaling group desired capacity to match dispatcher
    int currentDispatcherDesiredCapacity =
        aemHelperService.getAutoScalingGroupDesiredCapacityForPublishDispatcher();
    int currentPublishDesiredCapacity =
        aemHelperService.getAutoScalingGroupDesiredCapacityForPublish();

    if (currentDispatcherDesiredCapacity == currentPublishDesiredCapacity) {
      // If desired capacity already the same, then don't do anything
      logger.info(
          "Desired capacity already matching for publish auto scaling group and it's dispatcher."
              + " No changes will be made");
    } else {
      logger.info(
          "Changing publish auto scaling group capacity of "
              + currentPublishDesiredCapacity
              + " to match dispatcher's capacity of "
              + currentDispatcherDesiredCapacity);
      aemHelperService.setAutoScalingGroupDesiredCapacityForPublish(
          currentDispatcherDesiredCapacity);
    }

    return true;
  }
}
