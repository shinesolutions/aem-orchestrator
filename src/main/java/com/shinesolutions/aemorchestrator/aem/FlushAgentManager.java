package com.shinesolutions.aemorchestrator.aem;

import com.shinesolutions.swaggeraem4j.ApiException;
import com.shinesolutions.swaggeraem4j.ApiResponse;
import com.shinesolutions.swaggeraem4j.api.SlingApi;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Convenience methods for managing AEM flush agents. A flush agent flushes the cache for a given
 * dispatcher.
 */
@Component
public class FlushAgentManager {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource private AemApiFactory aemApiFactory;

  @Resource private AgentRequestFactory agentRequestFactory;

  @Resource private AemApiHelper aemApiHelper;

  /**
   * Deletes the flush agent for the given dispatcher from the AEM instance at {@code aemBaseUrl}.
   *
   * @param dispatcherInstanceId EC2 instance id of the dispatcher, used to derive the agent name
   * @param aemBaseUrl base URL of the AEM instance hosting the flush agent
   */
  public void deleteFlushAgent(String dispatcherInstanceId, String aemBaseUrl, AgentRunMode runMode)
      throws ApiException {
    logger.info(
        "Deleting flush agent for dispatcher id: "
            + dispatcherInstanceId
            + ", and run mode: "
            + runMode.getValue());

    PostAgentWithHttpInfoRequest request =
        agentRequestFactory.getDeleteAgentRequest(runMode, getFlushAgentName(dispatcherInstanceId));

    SlingApi slingApi = aemApiFactory.getSlingApi(aemBaseUrl, AgentAction.DELETE);

    ApiResponse<Void> response = aemApiHelper.postAgentWithHttpInfo(slingApi, request);

    logger.debug("ApiResponse status code: " + response.getStatusCode());
  }

  /**
   * Creates a flush agent on the AEM instance at {@code aemBaseUrl} that invalidates the cache of
   * the given dispatcher, retrying up to 5 times on {@link ApiException}.
   *
   * @param dispatcherInstanceId EC2 instance id of the dispatcher, used to derive the agent name
   * @param aemBaseUrl base URL of the AEM instance hosting the flush agent
   * @param aemDispatcherBaseUrl base URL of the dispatcher whose cache the agent flushes
   */
  @Retryable(maxAttempts = 5, retryFor = ApiException.class, backoff = @Backoff(delay = 5000))
  public void createFlushAgent(
      String dispatcherInstanceId,
      String aemBaseUrl,
      String aemDispatcherBaseUrl,
      AgentRunMode runMode)
      throws ApiException {
    logger.info(
        "Creating flush agent for dispatcher id: "
            + dispatcherInstanceId
            + ", and run mode: "
            + runMode.getValue());

    String agentDescription = "Flush Agent for author-dispatcher " + dispatcherInstanceId;

    PostAgentWithHttpInfoRequest request =
        agentRequestFactory.getCreateFlushAgentRequest(
            runMode,
            getFlushAgentName(dispatcherInstanceId),
            agentDescription,
            aemDispatcherBaseUrl);

    SlingApi slingApi = aemApiFactory.getSlingApi(aemBaseUrl, AgentAction.CREATE);

    ApiResponse<Void> response = aemApiHelper.postAgentWithHttpInfo(slingApi, request);

    logger.debug("ApiResponse status code: " + response.getStatusCode());
  }

  private String getFlushAgentName(String instanceId) {
    return "flushAgent-" + instanceId;
  }
}
