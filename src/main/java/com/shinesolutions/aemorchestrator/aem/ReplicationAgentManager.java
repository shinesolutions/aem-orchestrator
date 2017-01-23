package com.shinesolutions.aemorchestrator.aem;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.shinesolutions.swaggeraem4j.ApiException;
import com.shinesolutions.swaggeraem4j.ApiResponse;
import com.shinesolutions.swaggeraem4j.api.SlingApi;

/**
 * Convenience class for replication agent actions. A replication agent manages the replication
 * of content between author and publisher
 */
@Component
public class ReplicationAgentManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Value("${aem.replicator.username}")
    private String replicatorUsername;
    
    @Value("${aem.replicator.password}")
    private String replicatorPassword;

    @Resource
    private AemApiFactory aemApiFactory;

    @Resource
    private AgentRequestFactory agentRequestFactory;

    @Resource
    private AemApiHelper aemApiHelper;
    
    public void createReplicationAgent(String publisherId, String publisherAemBaseUrl, String authorAemBaseUrl,
        AgentRunMode runMode) throws ApiException {
        
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getCreateReplicationAgentRequest(
            runMode, getReplicationAgentName(publisherId), publisherAemBaseUrl, replicatorUsername, replicatorPassword);
        
        SlingApi slingApi = aemApiFactory.getSlingApi(authorAemBaseUrl, AgentAction.CREATE);
        
        ApiResponse<Void> response = aemApiHelper.postAgentWithHttpInfo(slingApi, request);
        
        logger.debug("ApiResponse status code: " + response.getStatusCode());
    }
    
    public void pauseReplicationAgent(String publisherId, String authorAemBaseUrl, 
        AgentRunMode runMode) throws ApiException {
        
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getPauseReplicationAgentRequest(
            runMode, getReplicationAgentName(publisherId), authorAemBaseUrl, "orchestrator-pause");
        
        SlingApi slingApi = aemApiFactory.getSlingApi(authorAemBaseUrl, AgentAction.PAUSE);
        
        ApiResponse<Void> response = aemApiHelper.postAgentWithHttpInfo(slingApi, request);
        
        logger.debug("ApiResponse status code: " + response.getStatusCode());
    }
    
    public void restartReplicationAgent(String publisherId, String authorAemBaseUrl, 
        AgentRunMode runMode) throws ApiException {
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getRestartReplicationAgentRequest(
            runMode, getReplicationAgentName(publisherId), authorAemBaseUrl, "admin");
        
        SlingApi slingApi = aemApiFactory.getSlingApi(authorAemBaseUrl, AgentAction.RESTART);
        
        ApiResponse<Void> response = aemApiHelper.postAgentWithHttpInfo(slingApi, request);
        
        logger.debug("ApiResponse status code: " + response.getStatusCode());
    }
    
    private String getReplicationAgentName(String instanceId) {
        return "replicationAgent-" + instanceId;
    }
}
