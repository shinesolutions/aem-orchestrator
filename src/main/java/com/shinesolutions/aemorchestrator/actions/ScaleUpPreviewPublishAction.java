package com.shinesolutions.aemorchestrator.actions;

import com.shinesolutions.aemorchestrator.aem.AgentRunMode;
import com.shinesolutions.aemorchestrator.aem.ReplicationAgentManager;
import com.shinesolutions.aemorchestrator.exception.InstanceNotInHealthyStateException;
import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import com.shinesolutions.swaggeraem4j.ApiException;
import jakarta.annotation.Resource;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Scale-up action run when a preview publish instance is launched. */
@Component
public class ScaleUpPreviewPublishAction implements Action {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Value("${aem.reverseReplication.enable}")
  private boolean enableReverseReplication;

  @Value("${aws.device.name}")
  private String awsDeviceName;

  @Resource private AemInstanceHelperService aemHelperService;

  @Resource private AwsHelperService awsHelperService;

  @Resource private ReplicationAgentManager replicationAgentManager;

  /**
   * Integrates a newly launched preview publish instance into the stack.
   *
   * <p>Runs the following steps in order, stopping at the first failure:
   *
   * <ol>
   *   <li>Finds an unpaired preview publish dispatcher and pairs it with the instance via tags.
   *   <li>Creates a preview replication agent on the author instance targeting the new preview
   *       publish instance, plus a preview reverse replication agent when {@code
   *       aem.reverseReplication.enable} is set.
   *   <li>Unless this is the first preview publish instance, waits for an active preview publish
   *       instance to be healthy, pauses its preview replication agent, snapshots its {@code
   *       aws.device.name} volume and resumes the agent. The new instance is tagged with the
   *       snapshot ID (empty for the first preview publish instance).
   *   <li>Creates a content health check alarm for the instance; a failure here is only logged.
   * </ol>
   *
   * @param instanceId the EC2 instance ID of the launched preview publish instance
   * @return {@code true} if all steps succeed, so the SQS message is removed from the queue; {@code
   *     false} otherwise, leaving the message on the queue to be retried
   */
  public boolean execute(String instanceId) {
    logger.info("ScaleUpPreviewPublishAction executing");

    // First create replication agent on previewPublish instance
    String authorAemBaseUrl = aemHelperService.getAemUrlForAuthorElb();
    String previewPublishAemBaseUrl = aemHelperService.getAemUrlForPreviewPublish(instanceId);

    // Find unpaired previewPublish dispatcher and pair it with tags
    boolean success = pairAndTagWithDispatcher(instanceId, authorAemBaseUrl);

    if (success) {
      success = prepareReplicationAgent(instanceId, authorAemBaseUrl, previewPublishAemBaseUrl);
    }

    // Create a new previewPublish from a snapshot of an active previewPublish instance
    if (success) {
      success = loadSnapshotFromActivePreviewPublisher(instanceId, authorAemBaseUrl);
    }

    if (success) {
      attachContentHealthCheckAlarm(instanceId);
    }

    return success;
  }

  private boolean prepareReplicationAgent(
      String instanceId, String authorAemBaseUrl, String previewPublishAemBaseUrl) {
    boolean success = true;
    try {
      replicationAgentManager.createPreviewReplicationAgent(
          instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

      if (enableReverseReplication) {
        logger.debug("Reverse replication is enabled");
        replicationAgentManager.createPreviewReverseReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);
      }

    } catch (ApiException e) {
      logger.error(
          "Error while attempting to create a new previewPublish replication agent for"
              + " previewPublish instance "
              + instanceId,
          e);
      success = false;
    }
    return success;
  }

  private boolean loadSnapshotFromActivePreviewPublisher(
      String instanceId, String authorAemBaseUrl) {
    boolean success = true;

    if (aemHelperService.isFirstPreviewPublishInstance()) {
      logger.info("First previewPublish instance, no snapshot needed");
      aemHelperService.tagInstanceWithSnapshotId(instanceId, ""); // Tag with empty Snapshot ID
    } else {
      String activePreviewPublishId =
          aemHelperService.getPreviewPublishIdToSnapshotFrom(instanceId);
      logger.debug("Active previewPublish instance id to snapshot from: " + activePreviewPublishId);

      logger.info(
          "Waiting for active previewPublish instance "
              + activePreviewPublishId
              + " to be in an healthy state");
      try {
        aemHelperService.waitForPreviewPublishToBeHealthy(activePreviewPublishId);
        logger.info(
            "Active previewPublish instance " + activePreviewPublishId + " is in a healthy state");
      } catch (InstanceNotInHealthyStateException e) {
        logger.warn(
            "Active previewPublish instance "
                + activePreviewPublishId
                + " is NOT in a healthy state. "
                + "Unable to take snapshot");
        success = false;
      }

      if (success) {
        success = performSnapshot(instanceId, activePreviewPublishId, authorAemBaseUrl);
      }
    }

    return success;
  }

  private boolean performSnapshot(
      String instanceId, String activePreviewPublishId, String authorAemBaseUrl) {
    boolean success = true;

    // Pause active previewPublish's replication agent before taking snapshot
    try {
      replicationAgentManager.pausePreviewReplicationAgent(
          activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);
      // Take snapshot
      String volumeId = awsHelperService.getVolumeId(activePreviewPublishId, awsDeviceName);

      logger.debug("Volume ID for snapshot: " + volumeId);

      if (volumeId != null) {
        String snapshotId =
            aemHelperService.createPreviewPublishSnapshot(activePreviewPublishId, volumeId);
        logger.info("Created snapshot with ID: " + snapshotId);

        aemHelperService.tagInstanceWithSnapshotId(instanceId, snapshotId);
      } else {
        logger.error(
            "Unable to find volume id for block device '"
                + awsDeviceName
                + "' and instance id "
                + activePreviewPublishId);
        success = false;
      }

    } catch (Exception e) {
      logger.error(
          "Error while pausing and attempting to snapshot an active preview Publish instance", e);
      success = false;
    } finally {
      // Always need to resume active previewPublish instance replication queue
      try {
        replicationAgentManager.resumePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);
      } catch (ApiException e) {
        logger.error(
            "Failed to restart replication queue for active previewPublish instance: "
                + activePreviewPublishId,
            e);
      }
    }

    return success;
  }

  private boolean pairAndTagWithDispatcher(String instanceId, String authorAemBaseUrl) {
    boolean success = true;

    try {
      // Find unpaired previewPublish dispatcher and pair it with tags
      logger.debug("Attempting to find unpaired Preview Publish dispatcher instance");
      String unpairedDispatcherId =
          aemHelperService.findUnpairedPreviewPublishDispatcher(instanceId);

      logger.debug(
          "Pairing previewPublish instance ("
              + instanceId
              + ") with preview publish dispatcher ("
              + unpairedDispatcherId
              + ") via tags");
      aemHelperService.pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

    } catch (NoSuchElementException nse) {
      logger.warn("Failed to find unpaired previewPublish dispatcher", nse);
      success = false;
    } catch (Exception e) {
      logger.error(
          "Error while attempting to pair previewPublish instance ("
              + instanceId
              + ") with dispatcher",
          e);
      success = false;
    }

    return success;
  }

  private void attachContentHealthCheckAlarm(String instanceId) {
    try {
      logger.info("Creating content health check alarm");
      aemHelperService.createContentHealthAlarmForPreviewPublisher(instanceId);
    } catch (Exception e) {
      logger.warn(
          "Failed to create content health check alarm for Preview Publish instance " + instanceId,
          e);
    }
  }
}
