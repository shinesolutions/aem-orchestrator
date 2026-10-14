package com.shinesolutions.aemorchestrator.actions;

import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Responds to a content health check CloudWatch alarm raised for a publish (or preview publish)
 * instance.
 *
 * <p>If {@code alarm.content.health.check.terminate.instance.enable} is set, the unhealthy instance
 * is terminated; otherwise its unhealthy state is only logged as an error. Failures are logged and
 * the action always returns {@code true}, so the alarm message is removed from the queue.
 */
@Component
public class AlarmContentHealthCheckAction implements Action {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Value("${alarm.content.health.check.terminate.instance.enable}")
  private boolean terminateInstanceEnable;

  @Resource private AwsHelperService awsHelperService;

  /*
   * Expects the instanceId to be the Publish instance ID
   */
  @Override
  public boolean execute(String instanceId) {
    logger.info("Executing AlarmContentHealthCheckAction");

    try {

      if (terminateInstanceEnable) {
        terminate(instanceId);
      } else {
        notify(instanceId);
      }

    } catch (Exception e) {
      logger.error(
          "Unable to terminate publish instance: " + instanceId + ". It may not be running?");
    }

    return true;
  }

  // Publish instance is in an unhealthy state, terminate it
  private void terminate(String instanceId) {
    awsHelperService.terminateInstance(instanceId);
    logger.info("Terminated publish instance " + instanceId);
  }

  // Publish instance is in an unhealthy state, notify
  private void notify(String instanceId) {
    String msg = String.format("Publish instance %s is in an unhealthy state", instanceId);
    logger.error(msg);
  }
}
