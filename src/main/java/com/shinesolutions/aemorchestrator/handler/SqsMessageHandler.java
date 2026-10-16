package com.shinesolutions.aemorchestrator.handler;

import com.shinesolutions.aemorchestrator.exception.MessageHandlerNotFoundException;
import com.shinesolutions.aemorchestrator.model.SnsMessage;
import com.shinesolutions.aemorchestrator.util.SnsMessageExtractor;
import jakarta.annotation.Resource;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Routes SNS notifications received from SQS to the {@link MessageHandler} for their message type.
 *
 * <p>The message type is taken from the SNS subject, which is matched by prefix against the keys of
 * the {@code eventTypeHandlerMappings} bean.
 */
@Component
public class SqsMessageHandler {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource private Map<String, MessageHandler> eventTypeHandlerMappings;

  @Resource private SnsMessageExtractor snsMessageExtractor;

  /**
   * Extracts the SNS notification from an SQS message and passes it to the matching handler.
   *
   * <p>Escaped quotes ({@code \"}) in the notification's message body are unescaped before it is
   * handed to the handler.
   *
   * @param message the JMS text message received from SQS, whose body is an SNS notification
   * @return {@code true} if the message should be acknowledged and removed from the queue: when its
   *     body cannot be read, when no handler matches its subject, or when the handler returns
   *     {@code true}. {@code false} if the subject is missing, the handler returns {@code false} or
   *     the handler throws.
   */
  public boolean handleMessage(Message message) {

    boolean deleteMessage = false;
    SnsMessage snsMessage = null;

    try {
      String messageBody = ((TextMessage) message).getText();
      logger.debug("Raw message body: " + messageBody);

      snsMessage = snsMessageExtractor.extractMessage(messageBody);

    } catch (Exception e) {
      logger.error("Error when attempting to read message", e);
      deleteMessage = true;
    }

    if (snsMessage != null && snsMessage.getSubject() != null) {
      // Get class mapping for message type:
      MessageHandler eventHandler = null;
      try {
        eventHandler = getHandler(snsMessage.getSubject());
      } catch (MessageHandlerNotFoundException e) {
        logger.error("Failed to find message handler", e);
      }

      if (eventHandler != null) {
        try {
          logger.debug("Handling event for subject: " + snsMessage.getSubject());
          deleteMessage = eventHandler.handleEvent(prepareMessageBody(snsMessage.getMessage()));

        } catch (Exception e) {
          logger.error(
              "Failed to handle event for message with subject: " + snsMessage.getSubject(), e);
        }
      } else {
        deleteMessage = true;
      }
    }

    return deleteMessage;
  }

  private MessageHandler getHandler(String msgSubject) throws MessageHandlerNotFoundException {
    String key =
        eventTypeHandlerMappings.keySet().stream()
            .filter(m -> msgSubject.startsWith(m))
            .findFirst()
            .orElseThrow(() -> new MessageHandlerNotFoundException(msgSubject));
    logger.debug("Using Message Handler for key: " + key);
    return eventTypeHandlerMappings.get(key);
  }

  private String prepareMessageBody(String messageBody) {
    // Body contains \" instead of just ". Need to replace before attempting to map to object
    return messageBody.replace("\\\"", "\"");
  }
}
