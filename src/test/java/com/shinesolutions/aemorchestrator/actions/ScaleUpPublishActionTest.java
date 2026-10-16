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
class ScaleUpPublishActionTest {

  @Mock private AemInstanceHelperService aemHelperService;

  @Mock private AwsHelperService awsHelperService;

  @Mock private ReplicationAgentManager replicationAgentManager;

  @InjectMocks private ScaleUpPublishAction action;

  private String awsDeviceName;
  private String instanceId;
  private String authorAemBaseUrl;
  private String publishAemBaseUrl;
  private String activePublishId;
  private String volumeId;
  private String snapshotId;
  private String unpairedDispatcherId;

  @BeforeEach
  void setUp() throws Exception {
    instanceId = "i-0347568433";
    awsDeviceName = "testDeviceName";
    authorAemBaseUrl = "testAuthorAemBaseUrl";
    publishAemBaseUrl = "testPublishAemBaseUrl";
    activePublishId = "testActivePublishId";
    volumeId = "testVolumeId";
    snapshotId = "testSnapshotId";
    unpairedDispatcherId = "testUnpairedDispatcherId";

    setField(action, "awsDeviceName", awsDeviceName);

    when(aemHelperService.getAemUrlForAuthorElb()).thenReturn(authorAemBaseUrl);
    when(aemHelperService.getAemUrlForPublish(instanceId)).thenReturn(publishAemBaseUrl);
    when(aemHelperService.findUnpairedPublishDispatcher(instanceId))
        .thenReturn(unpairedDispatcherId);
  }

  @Test
  void testExecuteSuccess() throws Exception {
    givenActivePublishToSnapshotFrom();
    givenActivePublishVolumeIsSnapshotted();

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(true));

    // Check replication agents
    verify(aemHelperService, times(1)).pairPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createReplicationAgent(
            instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(replicationAgentManager, times(0))
        .createReverseReplicationAgent(
            instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(replicationAgentManager, times(1))
        .pauseReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(aemHelperService, times(1)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(1))
        .resumeReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);
  }

  @Test
  void testExecuteReverseReplicationSuccess() throws Exception {
    givenActivePublishToSnapshotFrom();
    givenActivePublishVolumeIsSnapshotted();

    enableReverseReplication();

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(true));

    verify(aemHelperService, times(1)).pairPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createReplicationAgent(
            instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(replicationAgentManager, times(1))
        .createReverseReplicationAgent(
            instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(replicationAgentManager, times(1))
        .pauseReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(aemHelperService, times(1)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(1))
        .resumeReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);
  }

  @Test
  void testExceptionWhenCreatingReplicationAgent() throws Exception {
    doThrow(new ApiException("Test"))
        .when(replicationAgentManager)
        .createReplicationAgent(
            instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(false));

    verify(aemHelperService, times(1)).pairPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createReplicationAgent(
            instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(replicationAgentManager, times(0))
        .pauseReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(0))
        .resumeReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);
  }

  @Test
  void testExceptionWhenCreatingReverseReplicationAgent() throws Exception {
    enableReverseReplication();

    doThrow(new ApiException("Test"))
        .when(replicationAgentManager)
        .createReverseReplicationAgent(
            instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(false));

    verify(aemHelperService, times(1)).pairPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createReplicationAgent(
            instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(replicationAgentManager, times(0))
        .pauseReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(0))
        .resumeReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);
  }

  @Test
  void testWhenIsFirstPublish() throws Exception {
    when(aemHelperService.isFirstPublishInstance()).thenReturn(true);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(true));

    verify(aemHelperService, times(1)).pairPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createReplicationAgent(
            instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(replicationAgentManager, times(0))
        .pauseReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(aemHelperService, times(1))
        .tagInstanceWithSnapshotId(instanceId, ""); // Ensure is empty SnapshotId tag

    verify(replicationAgentManager, times(0))
        .resumeReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);
  }

  @Test
  void testCantFindHealthyActivePublisher() throws Exception {
    givenActivePublishToSnapshotFrom();

    doThrow(new InstanceNotInHealthyStateException(""))
        .when(aemHelperService)
        .waitForPublishToBeHealthy(activePublishId);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(false));

    verify(aemHelperService, times(1)).pairPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createReplicationAgent(
            instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(replicationAgentManager, times(0))
        .pauseReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(0))
        .resumeReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);
  }

  @Test
  void testExceptionWhenPausingActiveReplicationAgent() throws Exception {
    givenActivePublishToSnapshotFrom();

    doThrow(new ApiException("Test"))
        .when(replicationAgentManager)
        .pauseReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(false));

    verify(aemHelperService, times(1)).pairPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createReplicationAgent(
            instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(1))
        .resumeReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);
  }

  @Test
  void testWithNullVolumeId() throws Exception {
    givenActivePublishToSnapshotFrom();

    when(awsHelperService.getVolumeId(activePublishId, awsDeviceName)).thenReturn(null);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(false));

    verify(aemHelperService, times(1)).pairPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createReplicationAgent(
            instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(replicationAgentManager, times(1))
        .pauseReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(1))
        .resumeReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);
  }

  @Test
  void testFailedToResumeActivePublishReplicationAgent() throws Exception {
    givenActivePublishToSnapshotFrom();
    givenActivePublishVolumeIsSnapshotted();

    doThrow(new ApiException("Test"))
        .when(replicationAgentManager)
        .resumeReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(true));

    verify(aemHelperService, times(1)).pairPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(1))
        .createReplicationAgent(
            instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(replicationAgentManager, times(1))
        .pauseReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(aemHelperService, times(1)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(1))
        .resumeReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);
  }

  @Test
  void testUnableToFindUnpairedDispatcher() throws Exception {
    when(aemHelperService.findUnpairedPublishDispatcher(instanceId))
        .thenThrow(new NoSuchElementException());

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(false));

    verify(aemHelperService, times(0)).pairPublishWithDispatcher(instanceId, unpairedDispatcherId);

    verify(replicationAgentManager, times(0))
        .createReplicationAgent(
            instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(replicationAgentManager, times(0))
        .pauseReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(0))
        .resumeReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);
  }

  @Test
  void testUnablePairPublishWithDispatcher() throws Exception {
    doThrow(new RuntimeException("Test"))
        .when(aemHelperService)
        .pairPublishWithDispatcher(instanceId, unpairedDispatcherId);

    boolean result = action.execute(instanceId);
    assertThat(result, equalTo(false));

    verify(replicationAgentManager, times(0))
        .createReplicationAgent(
            instanceId, publishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(replicationAgentManager, times(0))
        .pauseReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);

    verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

    verify(replicationAgentManager, times(0))
        .resumeReplicationAgent(activePublishId, authorAemBaseUrl, AgentRunMode.PUBLISH);
  }

  /** Not the first publish instance: an active publish exists to snapshot from. */
  private void givenActivePublishToSnapshotFrom() throws Exception {
    when(aemHelperService.isFirstPublishInstance()).thenReturn(false);
    when(aemHelperService.getPublishIdToSnapshotFrom(instanceId)).thenReturn(activePublishId);
  }

  /** The active publish instance's data volume is found and snapshotted. */
  private void givenActivePublishVolumeIsSnapshotted() {
    when(awsHelperService.getVolumeId(activePublishId, awsDeviceName)).thenReturn(volumeId);
    when(aemHelperService.createPublishSnapshot(activePublishId, volumeId)).thenReturn(snapshotId);
  }

  private void enableReverseReplication() {
    setField(action, "enableReverseReplication", true);
  }
}
