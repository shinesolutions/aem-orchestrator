package com.shinesolutions.aemorchestrator.exception;

/** Thrown when no {@code MessageHandler} is mapped to an SNS notification's subject. */
public class MessageHandlerNotFoundException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception for the given unmatched subject.
   *
   * @param messageSubject the SNS subject that no handler mapping matched
   */
  public MessageHandlerNotFoundException(String messageSubject) {
    super("No message handler found for message with subject: " + messageSubject);
  }
}
