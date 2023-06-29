package com.shinesolutions.aemorchestrator.aem;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.model.AemCredentials;
import com.shinesolutions.swaggeraem4j.ApiException;
import com.shinesolutions.swaggeraem4j.ApiResponse;
import com.shinesolutions.swaggeraem4j.api.SlingApi;

/**
 * Convenience class for replication agent actions. A replication agent manages
 * the replication of content between author and publish instance
 */
@Component
public class ReplicationAgentManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private AemCredentials aemCredentials;

    @Resource
    private AemApiFactory aemApiFactory;

    @Resource
    private AgentRequestFactory agentRequestFactory;

    @Resource
    private AemApiHelper aemApiHelper;

    public void createReplicationAgent(String publishId, String publishAemBaseUrl, String authorAemBaseUrl,
        AgentRunMode runMode) throws ApiException {
        logger.info("Creating replication agent for publish id: " + publishId);
        
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getCreateReplicationAgentRequest(runMode,
            getReplicationAgentName(publishId), 
            "Replication agent for publish " + publishId, 
            publishAemBaseUrl, 
            aemCredentials.getReplicatorCredentials().getUserName(), 
            aemCredentials.getReplicatorCredentials().getPassword());

        performPostAgentAction(request, authorAemBaseUrl, AgentAction.CREATE);
    }

    public void createPreviewReplicationAgent(String publishId, String publishAemBaseUrl, String authorAemBaseUrl,
        AgentRunMode runMode) throws ApiException {
        logger.info("Creating replication agent for preview publish id: " + publishId);
        
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getCreateReplicationAgentRequest(runMode,
            getPreviewReplicationAgentName(publishId), 
            "Replication agent for preview publish " + publishId, 
            publishAemBaseUrl, 
            aemCredentials.getReplicatorCredentials().getUserName(), 
            aemCredentials.getReplicatorCredentials().getPassword());

        performPostAgentAction(request, authorAemBaseUrl, AgentAction.CREATE);
    }
    
    public void createReverseReplicationAgent(String publishId, String publishAemBaseUrl, String authorAemBaseUrl,
        AgentRunMode runMode) throws ApiException {
        logger.info("Creating reverse replication agent for publish id: " + publishId);
        
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getCreateReverseReplicationAgentRequest(runMode,
            getReverseReplicationAgentName(publishId), 
            "Reverse replication agent for publish " + publishId, 
            publishAemBaseUrl, 
            aemCredentials.getReplicatorCredentials().getUserName(), 
            aemCredentials.getReplicatorCredentials().getPassword());

        performPostAgentAction(request, authorAemBaseUrl, AgentAction.CREATE);
    }
    
    public void createPreviewReverseReplicationAgent(String publishId, String publishAemBaseUrl, String authorAemBaseUrl,
        AgentRunMode runMode) throws ApiException {
        logger.info("Creating reverse replication agent for preview publish id: " + publishId);
        
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getCreateReverseReplicationAgentRequest(runMode,
            getPreviewReverseReplicationAgentName(publishId), 
            "Reverse replication agent for preview publish " + publishId, 
            publishAemBaseUrl, 
            aemCredentials.getReplicatorCredentials().getUserName(), 
            aemCredentials.getReplicatorCredentials().getPassword());

        performPostAgentAction(request, authorAemBaseUrl, AgentAction.CREATE);
    }

    public void pauseReplicationAgent(String publishId, String authorAemBaseUrl, AgentRunMode runMode)
        throws ApiException {
        logger.info("Pausing replication agent for publish id: " + publishId);

        PostAgentWithHttpInfoRequest request = agentRequestFactory.getPauseReplicationAgentRequest(runMode,
            getReplicationAgentName(publishId));

        performPostAgentAction(request, authorAemBaseUrl, AgentAction.PAUSE);
    }

    public void pausePreviewReplicationAgent(String publishId, String authorAemBaseUrl, AgentRunMode runMode)
        throws ApiException {
        logger.info("Pausing replication agent for preview publish id: " + publishId);

        PostAgentWithHttpInfoRequest request = agentRequestFactory.getPauseReplicationAgentRequest(runMode,
            getPreviewReplicationAgentName(publishId));

        performPostAgentAction(request, authorAemBaseUrl, AgentAction.PAUSE);
    }

    public void resumeReplicationAgent(String publishId, String authorAemBaseUrl, AgentRunMode runMode)
        throws ApiException {
        logger.info("Resuming replication agent for publish id: " + publishId);
        
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getResumeReplicationAgentRequest(runMode,
            getReplicationAgentName(publishId), 
            aemCredentials.getReplicatorCredentials().getUserName(), 
            aemCredentials.getReplicatorCredentials().getPassword());

        performPostAgentAction(request, authorAemBaseUrl, AgentAction.RESTART);
    }

    public void resumePreviewReplicationAgent(String publishId, String authorAemBaseUrl, AgentRunMode runMode)
        throws ApiException {
        logger.info("Resuming replication agent for preview publish id: " + publishId);
        
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getResumeReplicationAgentRequest(runMode,
            getPreviewReplicationAgentName(publishId), 
            aemCredentials.getReplicatorCredentials().getUserName(), 
            aemCredentials.getReplicatorCredentials().getPassword());

        performPostAgentAction(request, authorAemBaseUrl, AgentAction.RESTART);
    }

    public void deleteReplicationAgent(String publishId, String authorAemBaseUrl, AgentRunMode runMode)
        throws ApiException {
        logger.info("Deleting replication agent for publish id: " + publishId);

        PostAgentWithHttpInfoRequest request = agentRequestFactory.getDeleteAgentRequest(runMode, 
            getReplicationAgentName(publishId));
        
        performPostAgentAction(request, authorAemBaseUrl, AgentAction.DELETE);
    }

    public void deletePreviewReplicationAgent(String publishId, String authorAemBaseUrl, AgentRunMode runMode)
        throws ApiException {
        logger.info("Deleting replication agent for preview publish id: " + publishId);

        PostAgentWithHttpInfoRequest request = agentRequestFactory.getDeleteAgentRequest(runMode, 
            getPreviewReplicationAgentName(publishId));
        
        performPostAgentAction(request, authorAemBaseUrl, AgentAction.DELETE);
    }
    
    public void deleteReverseReplicationAgent(String publishId, String authorAemBaseUrl, AgentRunMode runMode)
        throws ApiException {
        logger.info("Deleting reverse replication agent for publish id: " + publishId);
        
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getDeleteAgentRequest(runMode,
            getReverseReplicationAgentName(publishId));
        
        performPostAgentAction(request, authorAemBaseUrl, AgentAction.DELETE);
    }
    
    public void deletePreviewReverseReplicationAgent(String publishId, String authorAemBaseUrl, AgentRunMode runMode)
        throws ApiException {
        logger.info("Deleting reverse replication agent for preview publish id: " + publishId);
        
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getDeleteAgentRequest(runMode,
            getPreviewReverseReplicationAgentName(publishId));
        
        performPostAgentAction(request, authorAemBaseUrl, AgentAction.DELETE);
    }
    
    @Retryable(maxAttempts=5, value=ApiException.class, backoff=@Backoff(delay=5000))
    private void performPostAgentAction(PostAgentWithHttpInfoRequest request, String authorAemBaseUrl, 
        AgentAction action) throws ApiException {
        SlingApi slingApi = aemApiFactory.getSlingApi(authorAemBaseUrl, action);

        ApiResponse<Void> response = aemApiHelper.postAgentWithHttpInfo(slingApi, request);

        logger.debug("ApiResponse status code: " + response.getStatusCode());
    }

    private String getReplicationAgentName(String instanceId) {
        return "replicationAgent-" + instanceId;
    }

    private String getPreviewReplicationAgentName(String instanceId) {
        return "previewReplicationAgent-" + instanceId;
    }
    
    private String getReverseReplicationAgentName(String instanceId) {
        return "reverseReplicationAgent-" + instanceId;
    }
    
    private String getPreviewReverseReplicationAgentName(String instanceId) {
        return "previewReverseReplicationAgent-" + instanceId;
    }
}
