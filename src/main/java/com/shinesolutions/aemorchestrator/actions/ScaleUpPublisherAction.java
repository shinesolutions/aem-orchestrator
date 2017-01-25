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
public class ScaleUpPublisherAction implements ScaleAction {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Resource
    private AemHelperService aemHelperService;

    @Resource
    private AwsHelperService awsHelperService;
    
    @Resource
    private ReplicationAgentManager replicationAgentManager;
    
    private static final String DEVICE_NAME = "/dev/sdb";

    public boolean execute(String instanceId) {
        logger.info("ScaleUpPublisherAction executing");
        
        boolean success = true;
        
        // First create replication agent on publisher
        String publisherAemBaseUrl = aemHelperService.getAemUrlForPublisher(instanceId);
        String authorAemBaseUrl = aemHelperService.getAemUrlForAuthorElb();
        try {
            replicationAgentManager.createReplicationAgent(
                instanceId, publisherAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);
            
            // Immediately pause agent
            replicationAgentManager.pauseReplicationAgent(instanceId, authorAemBaseUrl, AgentRunMode.PUBLISH);
        } catch (ApiException e) {
            logger.error("Error while attempting to set up new publisher replication agent", e);
            success = false;
        }
        
        // Create a new publisher from a snapshot of an active publisher
        if(success) {
            String activePublisherId = aemHelperService.getPublisherIdToSnapshotFrom(instanceId);
            // Pause active publisher's replication agent before taking snapshot
            try {
                replicationAgentManager.pauseReplicationAgent(activePublisherId, authorAemBaseUrl, AgentRunMode.PUBLISH);
                // Take snapshot
                String volumeId = awsHelperService.getVolumeId(activePublisherId, DEVICE_NAME);
                if(volumeId != null) {
                    String snapshotShotId = awsHelperService.createSnapshot(volumeId, 
                        "Snapshot of publisher " + activePublisherId + " and volume id " + volumeId);
                    aemHelperService.tagInstanceWithSnapshotId(instanceId, snapshotShotId);
                    
                } else {
                    // Not good
                    logger.error("Unable to find volume id for block device '" + DEVICE_NAME + 
                        "' and instance id " + activePublisherId);
                    success = false;
                }
                
            } catch (ApiException e) {
                logger.error("Error while pausing and attempting to snapshot an active publisher", e);
                success = false;
            }
        }
        
        // Find unpaired publisher dispatcher and pair it with tags
        if(success) {
            String unpairedDispatcherId = aemHelperService.findUnpairedPublisherDispatcher();
            aemHelperService.pairPublisherWithDispatcher(instanceId, unpairedDispatcherId);
            
            // Resume paused replication agents
            try {
                replicationAgentManager.restartReplicationAgent(instanceId, authorAemBaseUrl, AgentRunMode.PUBLISH);
            } catch (ApiException e) {
                logger.error("Error while attempting to restart replication agent on publisher: " + instanceId);
            }
        }
        
        return success;
    }

}
