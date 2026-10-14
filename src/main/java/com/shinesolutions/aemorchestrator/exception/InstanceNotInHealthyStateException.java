package com.shinesolutions.aemorchestrator.exception;

/**
 * Thrown when an AEM instance does not pass its health check within the designated time.
 *
 * <p>Used when waiting for an active publish or preview publish instance to become healthy before
 * snapshotting it.
 */
public class InstanceNotInHealthyStateException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception for the given unhealthy instance.
   *
   * @param instanceId the EC2 instance ID that failed its health check
   */
  public InstanceNotInHealthyStateException(String instanceId) {
    super(getMessage(instanceId));
  }

  /**
   * Creates the exception for the given unhealthy instance with the error raised by the check.
   *
   * @param instanceId the EC2 instance ID that failed its health check
   * @param cause the error raised while checking the instance's health
   */
  public InstanceNotInHealthyStateException(String instanceId, Throwable cause) {
    super(getMessage(instanceId), cause);
  }

  private static String getMessage(String instanceId) {
    return "Instance with id "
        + instanceId
        + " is not responding to health checks within the designated time";
  }
}
