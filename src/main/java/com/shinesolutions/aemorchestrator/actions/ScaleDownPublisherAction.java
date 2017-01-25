package com.shinesolutions.aemorchestrator.actions;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.aem.AgentRunMode;
import com.shinesolutions.aemorchestrator.aem.ReplicationAgentManager;
import com.shinesolutions.aemorchestrator.service.AemHelperService;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import com.shinesolutions.swaggeraem4j.ApiException;

@Component
public class ScaleDownPublisherAction implements ScaleAction {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Resource
    private AemHelperService aemHelperService;

    @Resource
    private AwsHelperService awsHelperService;
    
    @Resource
    private ReplicationAgentManager replicationAgentManager;

    public boolean execute(String instanceId) {
        logger.info("ScaleDownPublisherAction executing");
        
        // Delete paired dispatcher
        String pairedDispatcherId = aemHelperService.getDispatcherIdForPairedPublisher(instanceId);
        if(pairedDispatcherId != null) {
            awsHelperService.terminateInstance(pairedDispatcherId);
        } else {
            logger.warn("Unable to find paired dispatcher for publisher " + instanceId);
        }
        
        // Delete replication agent on author
        String authorAemBaseUrl = aemHelperService.getAemUrlForAuthorElb();
        
        try {
            replicationAgentManager.deleteReplicationAgent(instanceId, authorAemBaseUrl, AgentRunMode.AUTHOR);
        } catch (ApiException e) {
            logger.error("Failed to delete replication agent on author for publisher id " + instanceId + 
                " and auth URL: " + authorAemBaseUrl, e);
        }
        
        return true;
    }

}
