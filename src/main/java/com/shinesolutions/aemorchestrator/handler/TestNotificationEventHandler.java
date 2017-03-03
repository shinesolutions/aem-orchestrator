package com.shinesolutions.aemorchestrator.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.model.EventMessage;

@Component
public class TestNotificationEventHandler implements EventHandler {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean handleEvent(EventMessage message) {
        logger.info("Test notification received, ignoring");
        return true;
    }

}
