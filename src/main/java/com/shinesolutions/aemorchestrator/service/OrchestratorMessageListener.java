package com.shinesolutions.aemorchestrator.service;

import com.amazon.sqs.javamessaging.SQSConnection;
import com.shinesolutions.aemorchestrator.handler.SqsMessageHandler;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Listener for the SQS queue. Will add itself as a message listener upon startup and then start the
 * connection.
 *
 * <p>When a message is received, it will pass it to the @see MessageHandler
 */
@Component
public class OrchestratorMessageListener implements MessageListener {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource private SQSConnection connection;

  @Resource private MessageConsumer consumer;

  @Resource private SqsMessageHandler messageHandler;

  @Override
  public void onMessage(Message message) {
    try {
      if (message != null) {
        logger.info("Message received " + message.getJMSMessageID());

        boolean removeMessageFromQueue = messageHandler.handleMessage(message);

        // Acknowledging the message with remove it from the queue
        if (removeMessageFromQueue) {
          logger.info("Acknowledged message (removing from queue): " + message.getJMSMessageID());
          message.acknowledge();
        } else {
          logger.info("Leaving message " + message.getJMSMessageID() + " on the queue");
        }
      } else {
        logger.info("Null message received");
      }

    } catch (Exception e) {
      logger.error("Error while recieving message", e);
    }
  }

  /**
   * Registers this listener on the SQS message consumer and starts the SQS connection.
   *
   * <p>Called on application startup once the Author ELB is healthy.
   *
   * @throws Exception if the listener cannot be registered or the connection fails to start
   */
  public void start() throws Exception {
    logger.debug("Initialising message receiver, starting SQS connection");
    consumer.setMessageListener(this);
    connection.start();
  }

  /**
   * Stops the SQS connection when the bean is destroyed.
   *
   * @throws Exception if the connection fails to stop
   */
  @PreDestroy
  public void cleanUp() throws Exception {
    logger.debug("Destroying message receiver, stopping SQS connection");
    connection.stop();
  }
}
