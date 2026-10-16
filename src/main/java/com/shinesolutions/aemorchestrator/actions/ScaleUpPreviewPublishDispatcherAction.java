package com.shinesolutions.aemorchestrator.actions;

import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Scale-up action run when a preview publish dispatcher instance is launched. */
@Component
public class ScaleUpPreviewPublishDispatcherAction implements Action {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource private AemInstanceHelperService aemHelperService;

  /**
   * Sets the preview publish Auto Scaling group's desired capacity to match the preview publish
   * dispatcher group's.
   *
   * <p>No change is made if the two desired capacities are already equal. The preview publish
   * instance launched as a result is paired with an unpaired dispatcher by {@link
   * ScaleUpPreviewPublishAction}.
   *
   * @param instanceId the EC2 instance ID of the launched preview publish dispatcher (not used)
   * @return always {@code true}, so the SQS message is removed from the queue
   */
  public boolean execute(String instanceId) {
    logger.info("ScaleUpPreviewPublishDispatcherAction executing");

    // Change previewPublish auto scaling group desired capacity to match dispatcher
    int currentDispatcherDesiredCapacity =
        aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublishDispatcher();
    int currentPreviewPublishDesiredCapacity =
        aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublish();

    if (currentDispatcherDesiredCapacity == currentPreviewPublishDesiredCapacity) {
      // If desired capacity already the same, then don't do anything
      logger.info(
          "Desired capacity already matching for previewPublish auto scaling group and it's"
              + " dispatcher. No changes will be made");
    } else {
      logger.info(
          "Changing previewPublish auto scaling group capacity of "
              + currentPreviewPublishDesiredCapacity
              + " to match dispatcher's capacity of "
              + currentDispatcherDesiredCapacity);
      aemHelperService.setAutoScalingGroupDesiredCapacityForPreviewPublish(
          currentDispatcherDesiredCapacity);
    }

    return true;
  }
}
