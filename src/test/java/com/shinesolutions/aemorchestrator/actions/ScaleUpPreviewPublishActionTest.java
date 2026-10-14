package com.shinesolutions.aemorchestrator.actions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.shinesolutions.aemorchestrator.aem.AgentRunMode;
import com.shinesolutions.aemorchestrator.aem.ReplicationAgentManager;
import com.shinesolutions.aemorchestrator.exception.InstanceNotInHealthyStateException;
import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import com.shinesolutions.swaggeraem4j.ApiException;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScaleUpPreviewPublishActionTest {

  @Mock private AemInstanceHelperService aemHelperService;

  @Mock private AwsHelperService awsHelperService;

  @Mock private ReplicationAgentManager replicationAgentManager;

  @InjectMocks private ScaleUpPreviewPublishAction action;

  private String awsDeviceName;
  private String instanceId;
  private String authorAemBaseUrl;
  private String previewPublishAemBaseUrl;
  private String activePreviewPublishId;
  private String volumeId;
  private String snapshotId;
  private String unpairedDispatcherId;

  @BeforeEach
  void setUp() throws Exception {
    instanceId = "i-0347568433";
    awsDeviceName = "testDeviceName";
    authorAemBaseUrl = "testAuthorAemBaseUrl";
    previewPublishAemBaseUrl = "testPreviewPublishAemBaseUrl";
    activePreviewPublishId = "testActivePreviewPublishId";
    volumeId = "testVolumeId";
    snapshotId = "testSnapshotId";
    unpairedDispatcherId = "testUnpairedDispatcherId";

    setField(action, "awsDeviceName", awsDeviceName);

    when(aemHelperService.getAemUrlForAuthorElb()).thenReturn(authorAemBaseUrl);
    when(aemHelperService.getAemUrlForPreviewPublish(instanceId))
        .thenReturn(previewPublishAemBaseUrl);
    when(aemHelperService.findUnpairedPreviewPublishDispatcher(instanceId))
        .thenReturn(unpairedDispatcherId);
  }

  @Test
  void testExecuteSuccess() throws Exception {
    givenActivePreviewPublishToSnapshotFrom();
    givenActivePreviewPublishVolumeIsSnapshotted();

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(true));

    // Check replication agents
    verify(aemHelperService, times(1))
        .pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createPreviewReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(replicationAgentManager, times(0))
        .createPreviewReverseReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(replicationAgentManager, times(1))
        .pausePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(aemHelperService, times(1)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(1))
        .resumePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);
  }

  @Test
  void testExecuteReverseReplicationSuccess() throws Exception {
    givenActivePreviewPublishToSnapshotFrom();
    givenActivePreviewPublishVolumeIsSnapshotted();

    enableReverseReplication();

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(true));

    verify(aemHelperService, times(1))
        .pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createPreviewReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(replicationAgentManager, times(1))
        .createPreviewReverseReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(replicationAgentManager, times(1))
        .pausePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(aemHelperService, times(1)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(1))
        .resumePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);
  }

  @Test
  void testExceptionWhenCreatingReplicationAgent() throws Exception {
    doThrow(new ApiException("Test"))
        .when(replicationAgentManager)
        .createPreviewReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(false));

    verify(aemHelperService, times(1))
        .pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createPreviewReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(replicationAgentManager, times(0))
        .pausePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(0))
        .resumePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);
  }

  @Test
  void testExceptionWhenCreatingReverseReplicationAgent() throws Exception {
    enableReverseReplication();

    doThrow(new ApiException("Test"))
        .when(replicationAgentManager)
        .createPreviewReverseReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(false));

    verify(aemHelperService, times(1))
        .pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createPreviewReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(replicationAgentManager, times(0))
        .pausePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(0))
        .resumePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);
  }

  @Test
  void testWhenIsFirstPreviewPublish() throws Exception {
    when(aemHelperService.isFirstPreviewPublishInstance()).thenReturn(true);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(true));

    verify(aemHelperService, times(1))
        .pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createPreviewReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(replicationAgentManager, times(0))
        .pausePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(aemHelperService, times(1))
        .tagInstanceWithSnapshotId(instanceId, ""); // Ensure is empty SnapshotId tag

    verify(replicationAgentManager, times(0))
        .resumePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);
  }

  @Test
  void testCantFindHealthyActivePreviewPublisher() throws Exception {
    givenActivePreviewPublishToSnapshotFrom();

    doThrow(new InstanceNotInHealthyStateException(""))
        .when(aemHelperService)
        .waitForPreviewPublishToBeHealthy(activePreviewPublishId);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(false));

    verify(aemHelperService, times(1))
        .pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createPreviewReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(replicationAgentManager, times(0))
        .pausePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(0))
        .resumePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);
  }

  @Test
  void testExceptionWhenPausingActiveReplicationAgent() throws Exception {
    givenActivePreviewPublishToSnapshotFrom();

    doThrow(new ApiException("Test"))
        .when(replicationAgentManager)
        .pausePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(false));

    verify(aemHelperService, times(1))
        .pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createPreviewReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(1))
        .resumePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);
  }

  @Test
  void testWithNullVolumeId() throws Exception {
    givenActivePreviewPublishToSnapshotFrom();

    when(awsHelperService.getVolumeId(activePreviewPublishId, awsDeviceName)).thenReturn(null);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(false));

    verify(aemHelperService, times(1))
        .pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createPreviewReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(replicationAgentManager, times(1))
        .pausePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(1))
        .resumePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);
  }

  @Test
  void testFailedToResumeActivePreviewPublishReplicationAgent() throws Exception {
    givenActivePreviewPublishToSnapshotFrom();
    givenActivePreviewPublishVolumeIsSnapshotted();

    doThrow(new ApiException("Test"))
        .when(replicationAgentManager)
        .resumePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(true));

    verify(aemHelperService, times(1))
        .pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createPreviewReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(replicationAgentManager, times(1))
        .pausePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(aemHelperService, times(1)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(1))
        .resumePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);
  }

  @Test
  void testUnableToFindUnpairedDispatcher() throws Exception {
    when(aemHelperService.findUnpairedPreviewPublishDispatcher(instanceId))
        .thenThrow(new NoSuchElementException());

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(false));

    verify(aemHelperService, times(0))
        .pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(0))
        .createPreviewReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(replicationAgentManager, times(0))
        .pausePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(0))
        .resumePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);
  }

  @Test
  void testUnablePairPreviewPublishWithDispatcher() throws Exception {
    doThrow(new RuntimeException("Test"))
        .when(aemHelperService)
        .pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(false));

    verify(replicationAgentManager, times(0))
        .createPreviewReplicationAgent(
            instanceId, previewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(replicationAgentManager, times(0))
        .pausePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

    verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(0))
        .resumePreviewReplicationAgent(
            activePreviewPublishId, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);
  }

  /** Not the first preview publish instance: an active one exists to snapshot from. */
  private void givenActivePreviewPublishToSnapshotFrom() throws Exception {
    when(aemHelperService.isFirstPreviewPublishInstance()).thenReturn(false);
    when(aemHelperService.getPreviewPublishIdToSnapshotFrom(instanceId))
        .thenReturn(activePreviewPublishId);
  }

  /** The active preview publish instance's data volume is found and snapshotted. */
  private void givenActivePreviewPublishVolumeIsSnapshotted() {
    when(awsHelperService.getVolumeId(activePreviewPublishId, awsDeviceName)).thenReturn(volumeId);
    when(aemHelperService.createPreviewPublishSnapshot(activePreviewPublishId, volumeId))
        .thenReturn(snapshotId);
  }

  private void enableReverseReplication() {
    setField(action, "enableReverseReplication", true);
  }
}
