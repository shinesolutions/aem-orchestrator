package com.shinesolutions.aemorchestrator.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TestNotificationEventHandler implements MessageHandler {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean handleEvent(String message) {
        logger.info("Test notification received, ignoring");
        return true;
    }

}
