package com.shinesolutions.aemorchestrator.actions;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.aem.AgentRunMode;
import com.shinesolutions.aemorchestrator.aem.ReplicationAgentManager;
import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import com.shinesolutions.swaggeraem4j.ApiException;

@Component
public class ScaleDownPublishAction implements ScaleAction {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Value("${aem.reverseReplication.enable}")
    private boolean reverseReplicationEnabled;
    
    @Resource
    private AemInstanceHelperService aemHelperService;

    @Resource
    private AwsHelperService awsHelperService;
    
    @Resource
    private ReplicationAgentManager replicationAgentManager;

    public boolean execute(String instanceId) {
        logger.info("ScaleDownPublishAction executing");
        
        boolean success = false;
        
        // Delete paired dispatcher
        String pairedDispatcherId = aemHelperService.getDispatcherIdForPairedPublish(instanceId);
        logger.debug("Paired publish dispatcher instance ID=" + pairedDispatcherId);
        
        if(pairedDispatcherId != null) {
            logger.info("Terminating paired publish dispatcher with ID: " + pairedDispatcherId);
            awsHelperService.terminateInstance(pairedDispatcherId);
        } else {
            logger.warn("Unable to find paired dispatcher for publish id " + instanceId);
        }
        
        // Delete replication agent on author
        String authorAemBaseUrl = aemHelperService.getAemUrlForAuthorElb();
        
        try {
            replicationAgentManager.deleteReplicationAgent(instanceId, authorAemBaseUrl, AgentRunMode.AUTHOR);
            success = true;
        } catch (ApiException e) {
            logger.error("Failed to delete replication agent on author for publish id " + instanceId + 
                " and auth URL: " + authorAemBaseUrl, e);
        }

        if (reverseReplicationEnabled) {
            logger.debug("Reverse replication is enabled");
            try {
                replicationAgentManager.deleteReverseReplicationAgent(instanceId, authorAemBaseUrl,
                    AgentRunMode.AUTHOR);

            } catch (ApiException e) {
                logger.warn("Failed to delete reverse replication agent on author for publish id " + instanceId
                    + " and auth URL: " + authorAemBaseUrl, e);
            }
        }
        
        return success;
    }

}
