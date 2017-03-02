package com.shinesolutions.aemorchestrator.actions;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;

@Component
public class ScaleDownPublishDispatcherAction implements ScaleAction {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private AemInstanceHelperService aemHelperService;

    @Resource
    private AwsHelperService awsHelperService;

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
            logger.warn("Unable to terminate paired publish instance with ID: " + pairedPublishId + 
                ". It may already be terminated");
        }

        // Change publish auto scaling group desired capacity to match dispatcher
        int currentDispatcherDesiredCapacity = aemHelperService
            .getAutoScalingGroupDesiredCapacityForPublishDispatcher();
        int currentPublishDesiredCapacity = aemHelperService.getAutoScalingGroupDesiredCapacityForPublish();

        if (currentDispatcherDesiredCapacity == currentPublishDesiredCapacity) {
            // If desired capacity already the same, then don't do anything
            logger.info("Desired capacity already matching for publish and dispatcher. No changes will be made");
        } else {
            logger.info("Changing publish auto scaling group capacity of " + currentPublishDesiredCapacity + 
                " to match dispatcher's capacity of " + currentDispatcherDesiredCapacity);
            aemHelperService.setAutoScalingGroupDesiredCapacityForPublish(currentDispatcherDesiredCapacity);
        }
        
        return true;
    }

}
