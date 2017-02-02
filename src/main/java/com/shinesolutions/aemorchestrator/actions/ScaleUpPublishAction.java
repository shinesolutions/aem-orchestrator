package com.shinesolutions.aemorchestrator.actions;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.aem.AgentRunMode;
import com.shinesolutions.aemorchestrator.aem.ReplicationAgentManager;
import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import com.shinesolutions.swaggeraem4j.ApiException;

@Component
public class ScaleUpPublishAction implements ScaleAction {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Resource
    private AemInstanceHelperService aemHelperService;

    @Resource
    private AwsHelperService awsHelperService;
    
    @Resource
    private ReplicationAgentManager replicationAgentManager;
    
    private static final String DEVICE_NAME = "/dev/sdb";

    public boolean execute(String instanceId) {
        logger.info("ScaleUpPublishAction executing");
        
        boolean success = true;
        
        // First create replication agent on publish instance
        String publishAemBaseUrl = aemHelperService.getAemUrlForPublish(instanceId);
        String authorAemBaseUrl = aemHelperService.getAemUrlForAuthorElb();
        try {
            replicationAgentManager.createReplicationAgent(
                instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);
            
            // Immediately pause agent
            replicationAgentManager.pauseReplicationAgent(instanceId, authorAemBaseUrl, AgentRunMode.PUBLISH);
        } catch (ApiException e) {
            logger.error("Error while attempting to set up new publish replication agent", e);
            success = false;
        }
        
        // Create a new publish from a snapshot of an active publish instance
        if(success) {
            String activePublishId = aemHelperService.getPublishIdToSnapshotFrom(instanceId);
            // Pause active publish's replication agent before taking snapshot
            try {
                replicationAgentManager.pauseReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);
                // Take snapshot
                String volumeId = awsHelperService.getVolumeId(activePublishId, DEVICE_NAME);
                if(volumeId != null) {
                    String snapshotShotId = awsHelperService.createSnapshot(volumeId, 
                        "Snapshot of publish instance id " + activePublishId + " and volume id " + volumeId);
                    aemHelperService.tagInstanceWithSnapshotId(instanceId, snapshotShotId);
                    
                } else {
                    // Not good
                    logger.error("Unable to find volume id for block device '" + DEVICE_NAME + 
                        "' and instance id " + activePublishId);
                    success = false;
                }
                
            } catch (ApiException e) {
                logger.error("Error while pausing and attempting to snapshot an active publish instance", e);
                success = false;
            }
        }
        
        // Find unpaired publish dispatcher and pair it with tags
        if(success) {
            String unpairedDispatcherId = aemHelperService.findUnpairedPublishDispatcher();
            aemHelperService.pairPublishWithDispatcher(instanceId, unpairedDispatcherId);
            
            // Resume paused replication agents
            try {
                replicationAgentManager.restartReplicationAgent(instanceId, authorAemBaseUrl, AgentRunMode.PUBLISH);
            } catch (ApiException e) {
                logger.error("Error while attempting to restart replication agent on publish instance: " + instanceId);
            }
        }
        
        return success;
    }

}
