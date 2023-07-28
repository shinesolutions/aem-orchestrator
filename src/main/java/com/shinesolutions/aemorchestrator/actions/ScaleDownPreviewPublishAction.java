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
public class ScaleDownPreviewPublishAction implements Action {

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
        logger.info("ScaleDownPreviewPublishAction executing");

        // Delete paired dispatcher
        String pairedDispatcherId = aemHelperService.getDispatcherIdForPairedPreviewPublish(instanceId);
        logger.debug("Paired previewPublish dispatcher instance ID=" + pairedDispatcherId);

        if(pairedDispatcherId != null) {
            logger.info("Terminating paired previewPublish dispatcher with ID: " + pairedDispatcherId);
            awsHelperService.terminateInstance(pairedDispatcherId);
        } else {
            logger.warn("Unable to terminate paired previewPublish dispatcher with ID: " + pairedDispatcherId +
                ". It may already be terminated");
        }

        // Delete replication agent on author
        String authorAemBaseUrl = aemHelperService.getAemUrlForAuthorElb();

        try {
            replicationAgentManager.deletePreviewReplicationAgent(instanceId, authorAemBaseUrl, AgentRunMode.AUTHOR);
        } catch (ApiException e) {
            logger.warn("Failed to delete replication agent on author for previewPublish id: " + instanceId +
                ". It may already be deleted.");
        }

        // Remove and reverse replication agents if they exist
        if (reverseReplicationEnabled) {
            logger.debug("Reverse replication is enabled");
            try {
                replicationAgentManager.deletePreviewReverseReplicationAgent(instanceId, authorAemBaseUrl,
                    AgentRunMode.AUTHOR);

            } catch (ApiException e) {
                logger.warn("Failed to delete reverse replication agent on author for previewPublish id " + instanceId
                    + ". It may already be deleted.");
            }
        }

        // Delete the attached content health check alarm
        try {
            aemHelperService.deleteContentHealthAlarmForPreviewPublisher(instanceId);
        } catch (Exception e) {
            logger.warn("Failed to remove content health check alarm for previewPublish instance " + instanceId, e);
        }

        return true;
    }

}
