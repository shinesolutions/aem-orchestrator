package com.shinesolutions.aemorchestrator.actions;

import com.shinesolutions.aemorchestrator.aem.AgentRunMode;
import com.shinesolutions.aemorchestrator.aem.FlushAgentManager;
import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.swaggeraem4j.ApiException;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Scale-up action run when an author dispatcher instance is launched. */
@Component
public class ScaleUpAuthorDispatcherAction implements Action {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource private FlushAgentManager flushAgentManager;

  @Resource private AemInstanceHelperService aemHelperService;

  /**
   * Registers a newly launched author dispatcher with the author instance.
   *
   * <p>Creates a flush agent on the author instance (via the author ELB) that targets the new
   * dispatcher, then tags the dispatcher with the author ELB DNS name as its author host.
   *
   * @param instanceId the EC2 instance ID of the launched author dispatcher
   * @return {@code true} if both steps succeed, so the SQS message is removed from the queue;
   *     {@code false} if either fails, leaving the message on the queue to be retried
   */
  public boolean execute(String instanceId) {
    logger.info("ScaleUpAuthorDispatcherAction executing");
    boolean success = false;

    String authDispatcherAemBaseUrl = aemHelperService.getAemUrlForAuthorDispatcher(instanceId);

    String authElbAemBaseUrl = aemHelperService.getAemUrlForAuthorElb();

    try {
      logger.debug("Attempting to create flush agent at base AEM path: " + authElbAemBaseUrl);

      flushAgentManager.createFlushAgent(
          instanceId, authElbAemBaseUrl, authDispatcherAemBaseUrl, AgentRunMode.AUTHOR);

      aemHelperService.tagAuthorDispatcherWithAuthorHost(instanceId);
      success = true;
    } catch (ApiException api) {
      logger.error(
          "Failed to create flush agent for dispatcher id: "
              + instanceId
              + ", and run mode: "
              + AgentRunMode.AUTHOR.getValue(),
          api);
    } catch (Exception e) {
      logger.error("Failed to add tags to author dispatcher", e);
    }

    return success;
  }
}
