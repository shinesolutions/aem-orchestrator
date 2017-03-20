package com.shinesolutions.aemorchestrator.actions;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;

@Component
public class ScaleUpPublishDispatcherAction implements Action {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Resource
    private AemInstanceHelperService aemHelperService;
    
    public boolean execute(String instanceId) {
        logger.info("ScaleUpPublishDispatcherAction executing");
        
        // Change publish auto scaling group desired capacity to match dispatcher
        int currentDispatcherDesiredCapacity = aemHelperService
            .getAutoScalingGroupDesiredCapacityForPublishDispatcher();
        int currentPublishDesiredCapacity = aemHelperService.getAutoScalingGroupDesiredCapacityForPublish();

        if (currentDispatcherDesiredCapacity == currentPublishDesiredCapacity) {
            // If desired capacity already the same, then don't do anything
            logger.info("Desired capacity already matching for publish auto scaling group and it's dispatcher. No changes will be made");
        } else {
            logger.info("Changing publish auto scaling group capacity of " + currentPublishDesiredCapacity + 
                " to match dispatcher's capacity of " + currentDispatcherDesiredCapacity);
            aemHelperService.setAutoScalingGroupDesiredCapacityForPublish(currentDispatcherDesiredCapacity);
        }
        
        return true;
    }

}
