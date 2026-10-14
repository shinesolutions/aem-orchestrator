package com.shinesolutions.aemorchestrator.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles Auto Scaling test notifications by logging and ignoring them.
 *
 * <p>Always returns {@code true}, so the notification is removed from the queue.
 */
@Component
public class TestNotificationEventHandler implements MessageHandler {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public boolean handleEvent(String message) {
    logger.debug("Raw message: " + message);

    logger.info("Test notification received, ignoring");
    return true;
  }
}
