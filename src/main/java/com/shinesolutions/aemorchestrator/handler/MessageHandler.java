package com.shinesolutions.aemorchestrator.handler;

/**
 * Handler for one type of SNS notification, selected by {@link SqsMessageHandler} from the
 * notification's subject.
 */
public interface MessageHandler {

  /**
   * Processes the message payload of an SNS notification.
   *
   * @param message the SNS notification's {@code Message} field (a JSON document)
   * @return {@code true} if the SQS message should be acknowledged and removed from the queue;
   *     {@code false} to leave it on the queue so it is redelivered and retried
   */
  boolean handleEvent(String message);
}
