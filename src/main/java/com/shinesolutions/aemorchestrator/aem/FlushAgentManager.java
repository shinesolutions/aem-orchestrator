package com.shinesolutions.aemorchestrator.aem;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.shinesolutions.swaggeraem4j.ApiException;
import com.shinesolutions.swaggeraem4j.ApiResponse;
import com.shinesolutions.swaggeraem4j.api.SlingApi;

/**
 * Convenience methods for managing AEM flush agents. A flush agent flushes the
 * cache for a given dispatcher.
 */
@Component
public class FlushAgentManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private AemApiFactory aemApiFactory;

    @Resource
    private AgentRequestFactory agentRequestFactory;

    @Resource
    private AemApiHelper aemApiHelper;

    public void deleteFlushAgent(String dispatcherInstanceId, String aemBaseUrl, AgentRunMode runMode)
        throws ApiException {
        logger.info(
            "Deleting flush agent for dispatcher id: " + dispatcherInstanceId + ", and run mode: " + runMode.getValue());

        SlingApi slingApi = aemApiFactory.getSlingApi(aemBaseUrl, AgentAction.DELETE);
        
        ApiResponse<Void> response = slingApi.deleteAgentWithHttpInfo(runMode.name().toLowerCase(),
            getFlushAgentName(dispatcherInstanceId));
        logger.debug("ApiResponse status code: " + response.getStatusCode());
    }

    @Retryable(maxAttempts=5, value=ApiException.class, backoff=@Backoff(delay=5000))
    public void createFlushAgent(String dispatcherInstanceId, String aemBaseUrl, String aemDispatcherBaseUrl,
        AgentRunMode runMode) throws ApiException {
        logger.info(
            "Creating flush agent for dispatcher id: " + dispatcherInstanceId + ", and run mode: " + runMode.getValue());
        
        String agentDescription = "Flush Agent for author-dispatcher " + dispatcherInstanceId;

        PostAgentWithHttpInfoRequest request = agentRequestFactory.getCreateFlushAgentRequest(runMode,
            getFlushAgentName(dispatcherInstanceId), agentDescription, aemDispatcherBaseUrl);

        SlingApi slingApi = aemApiFactory.getSlingApi(aemBaseUrl, AgentAction.CREATE);

        ApiResponse<Void> response = aemApiHelper.postAgentWithHttpInfo(slingApi, request);

        logger.debug("ApiResponse status code: " + response.getStatusCode());
    }

    private String getFlushAgentName(String instanceId) {
        return "flushAgent-" + instanceId;
    }

}
