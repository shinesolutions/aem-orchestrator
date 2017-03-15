package com.shinesolutions.aemorchestrator.actions;

import java.util.NoSuchElementException;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.aem.AgentRunMode;
import com.shinesolutions.aemorchestrator.aem.ReplicationAgentManager;
import com.shinesolutions.aemorchestrator.exception.InstanceNotInHealthyState;
import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import com.shinesolutions.swaggeraem4j.ApiException;

@Component
public class ScaleUpPublishAction implements ScaleAction {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Value("${aem.reverseReplication.enable}")
    private boolean enableReverseReplication;
    
    @Value("${aws.device.name}")
    private String awsDeviceName;
    
    @Resource
    private AemInstanceHelperService aemHelperService;

    @Resource
    private AwsHelperService awsHelperService;

    @Resource
    private ReplicationAgentManager replicationAgentManager;


    public boolean execute(String instanceId) {
        logger.info("ScaleUpPublishAction executing");

        // First create replication agent on publish instance
        String authorAemBaseUrl = aemHelperService.getAemUrlForAuthorElb();
        String publishAemBaseUrl = aemHelperService.getAemUrlForPublish(instanceId);

        // Find unpaired publish dispatcher and pair it with tags
        boolean success = pairAndTagWithDispatcher(instanceId, authorAemBaseUrl);

        if (success) {
            success = prepareReplicationAgent(instanceId, authorAemBaseUrl, publishAemBaseUrl);
        }
        
        // Create a new publish from a snapshot of an active publish instance
        if (success) {
            success = loadSnapshotFromActivePublisher(instanceId, authorAemBaseUrl);
        }

        return success;
    }
    
    
    private boolean prepareReplicationAgent(String instanceId, String authorAemBaseUrl, String publishAemBaseUrl) {
        boolean success = true;
        try {
            replicationAgentManager.createReplicationAgent(instanceId, publishAemBaseUrl, authorAemBaseUrl,
                AgentRunMode.PUBLISH);
 
            if(enableReverseReplication) {
                logger.debug("Reverse replication is enabled");
                replicationAgentManager.createReverseReplicationAgent(instanceId, publishAemBaseUrl, 
                    authorAemBaseUrl, AgentRunMode.PUBLISH);
            }
            
        } catch (ApiException e) {
            logger.error("Error while attempting to create a new publish replication agent on publish instance "
                + instanceId, e);
            success = false;
        }
        return success;
    }
    
    
    private boolean loadSnapshotFromActivePublisher(String instanceId, String authorAemBaseUrl) {
        boolean success = true;
        
        if(aemHelperService.isFirstPublishInstance()) {
            aemHelperService.tagInstanceWithSnapshotId(instanceId, ""); //Tag with empty Snapshot ID
        } else {
            String activePublishId = aemHelperService.getPublishIdToSnapshotFrom(instanceId);
            logger.debug("Active publish instance id to snapshot from: " + activePublishId);
            
            logger.info("Waiting for active publish instance " + activePublishId + " to be in an healthy state");
            try {
                aemHelperService.waitForPublishToBeHealthy(activePublishId);
                logger.info("Active publish instance " + activePublishId + " is in a healthy state");
            } catch (InstanceNotInHealthyState e) {
                logger.warn("Active publish instance " + activePublishId + " is NOT in a healthy state. "
                    + "Unable to take snapshot");
                success = false;
            }
            
            if(success) {
                success = performSnapshot(instanceId, activePublishId, authorAemBaseUrl);
            }
        } 
        
        return success;
    }
    
    private boolean performSnapshot(String instanceId, String activePublishId, String authorAemBaseUrl) {
        boolean success = true;
        
        // Pause active publish's replication agent before taking snapshot
        try {
            replicationAgentManager.pauseReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);
            // Take snapshot
            String volumeId = awsHelperService.getVolumeId(activePublishId, awsDeviceName);
            
            logger.debug("Volume ID for snapshot: " + volumeId);
            
            if (volumeId != null) {
                String snapshotId = aemHelperService.createPublishSnapshot(activePublishId, volumeId);
                logger.debug("Snapshot ID: " + snapshotId);
                
                aemHelperService.tagInstanceWithSnapshotId(instanceId, snapshotId);
            } else {
                logger.error("Unable to find volume id for block device '" + awsDeviceName + "' and instance id "
                    + activePublishId);
                success = false;
            }

        } catch (Exception e) {
            logger.error("Error while pausing and attempting to snapshot an active publish instance", e);
            success = false;
        } finally {
            // Always need to resume active publish instance replication queue
            try {
                replicationAgentManager.resumeReplicationAgent(activePublishId, authorAemBaseUrl, 
                        AgentRunMode.PUBLISH);
            } catch (ApiException e) {
                logger.error("Failed to restart replication queue for active publish instance: " + activePublishId, e);
            }
        }
        
        return success;
    }
    
    
    private boolean pairAndTagWithDispatcher(String instanceId, String authorAemBaseUrl) {
        boolean success = true;
        
        try {
            // Find unpaired publish dispatcher and pair it with tags
            logger.debug("Attempting to find unpaired publish dispatcher instance");
            String unpairedDispatcherId = aemHelperService.findUnpairedPublishDispatcher();

            logger.debug("Pairing publish instance (" + instanceId + ") with pubish dispatcher ("
                + unpairedDispatcherId + ") via tags");
            aemHelperService.pairPublishWithDispatcher(instanceId, unpairedDispatcherId);

        } catch (NoSuchElementException nse) {
            logger.warn("Failed to find unpaired publish dispatcher", nse);
            success = false;
        } catch (Exception e) {
            logger.error("Error while attempting to pair publish instance (" + 
                instanceId + ") with dispatcher", e);
            success = false;
        }
        
        return success;
    }

}
