package com.shinesolutions.aemorchestrator.actions;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;

@Component
public class ScaleUpPreviewPublishDispatcherAction implements Action {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private AemInstanceHelperService aemHelperService;

    public boolean execute(String instanceId) {
        logger.info("ScaleUpPreviewPublishDispatcherAction executing");

        // Change previewPublish auto scaling group desired capacity to match dispatcher
        int currentDispatcherDesiredCapacity = aemHelperService
            .getAutoScalingGroupDesiredCapacityForPreviewPublishDispatcher();
        int currentPreviewPublishDesiredCapacity = aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublish();

        if (currentDispatcherDesiredCapacity == currentPreviewPublishDesiredCapacity) {
            // If desired capacity already the same, then don't do anything
            logger.info("Desired capacity already matching for previewPublish auto scaling group and it's dispatcher. No changes will be made");
        } else {
            logger.info("Changing previewPublish auto scaling group capacity of " + currentPreviewPublishDesiredCapacity +
                " to match dispatcher's capacity of " + currentDispatcherDesiredCapacity);
            aemHelperService.setAutoScalingGroupDesiredCapacityForPreviewPublish(currentDispatcherDesiredCapacity);
        }

        return true;
    }

}
