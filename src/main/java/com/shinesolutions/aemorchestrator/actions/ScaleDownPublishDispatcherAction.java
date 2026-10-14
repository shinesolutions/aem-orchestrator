package com.shinesolutions.aemorchestrator.actions;

import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Scale-down action run when a publish dispatcher instance is terminated. */
@Component
public class ScaleDownPublishDispatcherAction implements Action {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource private AemInstanceHelperService aemHelperService;

  @Resource private AwsHelperService awsHelperService;

  /**
   * Terminates the paired publish instance and realigns the publish Auto Scaling group capacity.
   *
   * <p>Terminates the publish instance paired with the dispatcher (if one is found), then sets the
   * publish Auto Scaling group's desired capacity to the publish dispatcher group's desired
   * capacity when the two differ.
   *
   * @param instanceId the EC2 instance ID of the terminated publish dispatcher
   * @return always {@code true}, so the SQS message is removed from the queue
   */
  public boolean execute(String instanceId) {
    logger.info("ScaleDownPublishDispatcherAction executing");

    // Find and terminate paired publish instance
    String pairedPublishId = aemHelperService.getPublishIdForPairedDispatcher(instanceId);
    logger.debug("Paired publish instance ID=" + pairedPublishId);

    if (pairedPublishId != null) {
      // Terminate paired publish instance
      logger.info("Terminating paired publish instance with ID: " + pairedPublishId);
      awsHelperService.terminateInstance(pairedPublishId);
    } else {
      logger.warn(
          "Unable to find paired publish instance for publish dispatcher with ID: "
              + instanceId
              + ". It may already be terminated");
    }

    // Change publish auto scaling group desired capacity to match dispatcher
    int currentDispatcherDesiredCapacity =
        aemHelperService.getAutoScalingGroupDesiredCapacityForPublishDispatcher();
    int currentPublishDesiredCapacity =
        aemHelperService.getAutoScalingGroupDesiredCapacityForPublish();

    if (currentDispatcherDesiredCapacity == currentPublishDesiredCapacity) {
      // If desired capacity already the same, then don't do anything
      logger.info(
          "Desired capacity already matching for publish and dispatcher. No changes will be made");
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
