package com.shinesolutions.aemorchestrator.exception;

/**
 * Thrown when no unpaired dispatcher instance is available to pair with a publish or preview
 * publish instance.
 */
public class NoPairFoundException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception for the instance that could not be paired.
   *
   * @param instanceId the EC2 instance ID of the publish or preview publish instance
   */
  public NoPairFoundException(String instanceId) {
    super("Unable to find available pair for instance " + instanceId);
  }
}
