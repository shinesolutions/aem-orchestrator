package com.shinesolutions.aemorchestrator.actions;

import com.shinesolutions.aemorchestrator.aem.AgentRunMode;
import com.shinesolutions.aemorchestrator.aem.FlushAgentManager;
import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.swaggeraem4j.ApiException;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Scale-down action run when an author dispatcher instance is terminated. */
@Component
public class ScaleDownAuthorDispatcherAction implements Action {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource private FlushAgentManager flushAgentManager;

  @Resource private AemInstanceHelperService aemHelperService;

  /**
   * Deletes the terminated dispatcher's flush agent from the author instance (via the author ELB).
   *
   * <p>A failure to delete the agent is logged and ignored, as it may already have been deleted.
   *
   * @param instanceId the EC2 instance ID of the terminated author dispatcher
   * @return always {@code true}, so the SQS message is removed from the queue
   */
  public boolean execute(String instanceId) {
    logger.info("ScaleDownAuthorDispatcherAction executing");

    String aemBasePath = aemHelperService.getAemUrlForAuthorElb();

    try {
      logger.debug("Attempting to delete flush agent at base AEM path: " + aemBasePath);
      flushAgentManager.deleteFlushAgent(instanceId, aemBasePath, AgentRunMode.AUTHOR);
      logger.info("Flush Agent removed successfully");
    } catch (ApiException e) {
      logger.warn(
          "Failed to delete flush agent for dispatcher id: "
              + instanceId
              + ". It may already have been deleted");
    }

    return true;
  }
}
