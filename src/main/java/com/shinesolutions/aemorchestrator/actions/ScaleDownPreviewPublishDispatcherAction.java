package com.shinesolutions.aemorchestrator.actions;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;

@Component
public class ScaleDownPreviewPublishDispatcherAction implements Action {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private AemInstanceHelperService aemHelperService;

    @Resource
    private AwsHelperService awsHelperService;

    public boolean execute(String instanceId) {
        logger.info("ScaleDownPreviewPublishDispatcherAction executing");

        // Find and terminate paired previewPublish instance
        String pairedPreviewPublishId = aemHelperService.getPreviewPublishIdForPairedDispatcher(instanceId);
        logger.debug("Paired previewPublish instance ID=" + pairedPreviewPublishId);

        if (pairedPreviewPublishId != null) {
            // Terminate paired previewPublish instance
            logger.info("Terminating paired previewPublish instance with ID: " + pairedPreviewPublishId);
            awsHelperService.terminateInstance(pairedPreviewPublishId);
        } else {
            logger.warn("Unable to terminate paired previewPublish instance with ID: " + pairedPreviewPublishId +
                ". It may already be terminated");
        }

        // Change previewPublish auto scaling group desired capacity to match dispatcher
        int currentDispatcherDesiredCapacity = aemHelperService
            .getAutoScalingGroupDesiredCapacityForPreviewPublishDispatcher();
        int currentPreviewPublishDesiredCapacity = aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublish();

        if (currentDispatcherDesiredCapacity == currentPreviewPublishDesiredCapacity) {
            // If desired capacity already the same, then don't do anything
            logger.info("Desired capacity already matching for previewPublish and dispatcher. No changes will be made");
        } else {
            logger.info("Changing previewPublish auto scaling group capacity of " + currentPreviewPublishDesiredCapacity +
                " to match dispatcher's capacity of " + currentDispatcherDesiredCapacity);
            aemHelperService.setAutoScalingGroupDesiredCapacityForPreviewPublish(currentDispatcherDesiredCapacity);
        }

        return true;
    }

}
