package com.shinesolutions.aemorchestrator.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ScaleDownPublisherAction implements ScaleAction {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public boolean execute(String instanceId) {
        logger.info("ScaleDownPublisherAction executing");
        
        return false;
    }

}
