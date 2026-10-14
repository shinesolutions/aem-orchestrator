package com.shinesolutions.aemorchestrator.actions;

/**
 * Orchestration step run for a single EC2 instance in response to an Auto Scaling event or a
 * CloudWatch alarm.
 *
 * <p>Implementations are selected by the message handlers, typically by mapping the Auto Scaling
 * group name of the event to the scale-up or scale-down action for that AEM component.
 */
public interface Action {

  /**
   * Performs the action for the given instance.
   *
   * @param instanceId the EC2 instance ID the triggering event refers to
   * @return {@code true} if the originating SQS message can be removed from the queue; {@code
   *     false} to leave it on the queue so it is redelivered and retried
   */
  boolean execute(String instanceId);
}
