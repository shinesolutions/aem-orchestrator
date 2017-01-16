package com.shinesolutions.aemorchestrator.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ScaleDownPublisherDispatcherAction implements ScaleAction {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public boolean execute(String instanceId) {
        logger.info("ScaleDownPublisherDispatcherAction executing");
        
        return false;
    }

}
