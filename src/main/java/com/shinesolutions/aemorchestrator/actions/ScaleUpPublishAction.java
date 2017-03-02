package com.shinesolutions.aemorchestrator.actions;

import java.util.NoSuchElementException;

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
            replicationAgentManager.createReplicationAgent(instanceId, publishAemBaseUrl, authorAemBaseUrl,
                AgentRunMode.PUBLISH);

            // Immediately pause agent
            replicationAgentManager.pauseReplicationAgent(instanceId, authorAemBaseUrl, AgentRunMode.PUBLISH);
        } catch (ApiException e) {
            logger.error("Error while attempting to set up new publish replication agent via author AEM URL: "
                + authorAemBaseUrl, e);
            success = false;
        }

        // Create a new publish from a snapshot of an active publish instance
        if (success) {
            String activePublishId = aemHelperService.getPublishIdToSnapshotFrom(instanceId);
            // Pause active publish's replication agent before taking snapshot
            try {
                replicationAgentManager.pauseReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);
                // Take snapshot
                String volumeId = awsHelperService.getVolumeId(activePublishId, DEVICE_NAME);
                logger.debug("Volume ID for snapshot: " + volumeId);
                if (volumeId != null) {
                    String snapshotShotId = awsHelperService.createSnapshot(volumeId,
                        "Orchestration AEM snapshot of publish instance " + activePublishId + " and volume " + volumeId);
                    aemHelperService.tagInstanceWithSnapshotId(instanceId, snapshotShotId);
                    logger.debug("Snapshot ID: " + snapshotShotId);
                } else {
                    // Not good
                    logger.error("Unable to find volume id for block device '" + DEVICE_NAME + "' and instance id "
                        + activePublishId);
                    success = false;
                }

            } catch (ApiException e) {
                logger.error("Error while pausing and attempting to snapshot an active publish instance", e);
                success = false;
            } finally {
                // Need to resume active publish instance replication queue
                try {
                    if(activePublishId != null) {
                        replicationAgentManager.resumeReplicationAgent(activePublishId, authorAemBaseUrl,
                            AgentRunMode.PUBLISH);
                    }
                } catch (ApiException e) {
                    logger.error("Failed to restart replication queue for active publish instance: " + activePublishId, e);
                }
            }
        }

        if (success) {
            try {
                // Find unpaired publish dispatcher and pair it with tags
                logger.debug("Attempting to find unpaired publish dispatcher instance");
                String unpairedDispatcherId = aemHelperService.findUnpairedPublishDispatcher();

                logger.debug("Pairing publish instance (" + instanceId + ") with pubish dispatcher ("
                    + unpairedDispatcherId + ") via tags");
                aemHelperService.pairPublishWithDispatcher(instanceId, unpairedDispatcherId);

                // Resume paused replication agents
                replicationAgentManager.resumeReplicationAgent(instanceId, authorAemBaseUrl, AgentRunMode.PUBLISH);
            } catch (NoSuchElementException nse) {
                logger.warn("Failed to find unpaired publish dispatcher", nse);
                success = false;
            } catch (ApiException e) {
                logger.error("Error while attempting to restart replication agent on publish instance: " +
                    instanceId, e);
            }
        }

        return success;
    }

}
