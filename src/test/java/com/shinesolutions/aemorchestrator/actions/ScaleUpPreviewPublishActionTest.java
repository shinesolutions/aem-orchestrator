package com.shinesolutions.aemorchestrator.actions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.shinesolutions.aemorchestrator.aem.AgentRunMode;
import com.shinesolutions.aemorchestrator.aem.ReplicationAgentManager;
import com.shinesolutions.aemorchestrator.exception.InstanceNotInHealthyStateException;
import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import com.shinesolutions.swaggeraem4j.ApiException;

@RunWith(MockitoJUnitRunner.class)
public class ScaleUpPreviewPublishActionTest {

    @Mock
    private AemInstanceHelperService aemHelperService;

    @Mock
    private AwsHelperService awsHelperService;

    @Mock
    private ReplicationAgentManager replicationAgentManager;

    @InjectMocks
    private ScaleUpPreviewPublishAction action;

    private String awsDeviceName;
    private String instanceId;
    private String authorAemBaseUrl;
    private String PreviewPublishAemBaseUrl;
    private String activePreviewPublishId;
    private String volumeId;
    private String snapshotId;
    private String unpairedDispatcherId;

    @Before
    public void setUp() throws Exception {
        instanceId = "i-0347568433";
        awsDeviceName = "testDeviceName";
        authorAemBaseUrl = "testAuthorAemBaseUrl";
        PreviewPublishAemBaseUrl = "testPreviewPublishAemBaseUrl";
        activePreviewPublishId = "testActivePreviewPublishId";
        volumeId = "testVolumeId";
        snapshotId = "testSnapshotId";
        unpairedDispatcherId = "testUnpairedDispatcherId";

        setField(action, "awsDeviceName", awsDeviceName);

        when(aemHelperService.getAemUrlForAuthorElb()).thenReturn(authorAemBaseUrl);
        when(aemHelperService.getAemUrlForPreviewPublish(instanceId)).thenReturn(PreviewPublishAemBaseUrl);
        when(aemHelperService.getPreviewPublishIdToSnapshotFrom(instanceId)).thenReturn(activePreviewPublishId);
        when(awsHelperService.getVolumeId(activePreviewPublishId, awsDeviceName)).thenReturn(volumeId);
        when(aemHelperService.createPreviewPublishSnapshot(activePreviewPublishId, volumeId)).thenReturn(snapshotId);
        when(aemHelperService.findUnpairedPreviewPublishDispatcher(instanceId)).thenReturn(unpairedDispatcherId);
        when(aemHelperService.isFirstPreviewPublishInstance()).thenReturn(false);
    }

    @Test
    public void testExecuteSuccess() throws Exception {
        boolean result = action.execute(instanceId);

        // Check replication agents
        verify(aemHelperService, times(1)).pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

        verify(replicationAgentManager, times(1)).createPreviewReplicationAgent(instanceId, PreviewPublishAemBaseUrl,
            authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        verify(replicationAgentManager, times(0)).createPreviewReverseReplicationAgent(instanceId, PreviewPublishAemBaseUrl,
            authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        verify(replicationAgentManager, times(1)).pausePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        verify(aemHelperService, times(1)).tagInstanceWithSnapshotId(instanceId, snapshotId);

        verify(replicationAgentManager, times(1)).resumePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        assertThat(result, equalTo(true));
    }

    @Test
    public void testExecuteReverseReplicationSuccess() throws Exception {
        enableReverseReplication();

        boolean result = action.execute(instanceId);

        verify(aemHelperService, times(1)).pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

        verify(replicationAgentManager, times(1)).createPreviewReplicationAgent(instanceId, PreviewPublishAemBaseUrl,
            authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        verify(replicationAgentManager, times(1)).createPreviewReverseReplicationAgent(instanceId, PreviewPublishAemBaseUrl,
            authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        verify(replicationAgentManager, times(1)).pausePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        verify(aemHelperService, times(1)).tagInstanceWithSnapshotId(instanceId, snapshotId);

        verify(replicationAgentManager, times(1)).resumePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        assertThat(result, equalTo(true));
    }

    @Test
    public void testExceptionWhenCreatingReplicationAgent() throws Exception {
        doThrow(new ApiException("Test")).when(replicationAgentManager).createPreviewReplicationAgent(instanceId,
            PreviewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        boolean result = action.execute(instanceId);

        verify(aemHelperService, times(1)).pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

        verify(replicationAgentManager, times(1)).createPreviewReplicationAgent(instanceId, PreviewPublishAemBaseUrl,
            authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        verify(replicationAgentManager, times(0)).pausePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

        verify(replicationAgentManager, times(0)).resumePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        assertThat(result, equalTo(false));
    }

    @Test
    public void testExceptionWhenCreatingReverseReplicationAgent() throws Exception {
        enableReverseReplication();

        doThrow(new ApiException("Test")).when(replicationAgentManager).createPreviewReverseReplicationAgent(instanceId,
            PreviewPublishAemBaseUrl, authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        boolean result = action.execute(instanceId);

        verify(aemHelperService, times(1)).pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

        verify(replicationAgentManager, times(1)).createPreviewReplicationAgent(instanceId, PreviewPublishAemBaseUrl,
            authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        verify(replicationAgentManager, times(0)).pausePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

        verify(replicationAgentManager, times(0)).resumePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        assertThat(result, equalTo(false));
    }

    @Test
    public void testWhenIsFirstPreviewPublish() throws Exception {
        when(aemHelperService.isFirstPreviewPublishInstance()).thenReturn(true);

        boolean result = action.execute(instanceId);

        verify(aemHelperService, times(1)).pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

        verify(replicationAgentManager, times(1)).createPreviewReplicationAgent(instanceId, PreviewPublishAemBaseUrl,
            authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        verify(replicationAgentManager, times(0)).pausePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        verify(aemHelperService, times(1)).tagInstanceWithSnapshotId(instanceId, ""); //Ensure is empty SnapshotId tag

        verify(replicationAgentManager, times(0)).resumePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        assertThat(result, equalTo(true));
    }

    @Test
    public void testCantFindHealthyActivePreviewPublisher() throws Exception {
        doThrow(new InstanceNotInHealthyStateException("")).when(aemHelperService).waitForPreviewPublishToBeHealthy(activePreviewPublishId);

        boolean result = action.execute(instanceId);

        verify(aemHelperService, times(1)).pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

        verify(replicationAgentManager, times(1)).createPreviewReplicationAgent(instanceId, PreviewPublishAemBaseUrl,
            authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        verify(replicationAgentManager, times(0)).pausePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

        verify(replicationAgentManager, times(0)).resumePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        assertThat(result, equalTo(false));
    }

    @Test
    public void testExceptionWhenPausingActiveReplicationAgent() throws Exception {
        doThrow(new ApiException("Test")).when(replicationAgentManager).pausePreviewReplicationAgent(activePreviewPublishId,
             authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        boolean result = action.execute(instanceId);

        verify(aemHelperService, times(1)).pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

        verify(replicationAgentManager, times(1)).createPreviewReplicationAgent(instanceId, PreviewPublishAemBaseUrl,
            authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

        verify(replicationAgentManager, times(1)).resumePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        assertThat(result, equalTo(false));
    }

    @Test
    public void testWithNullVolumeId() throws Exception {
        when(awsHelperService.getVolumeId(activePreviewPublishId, awsDeviceName)).thenReturn(null);

        boolean result = action.execute(instanceId);

        verify(aemHelperService, times(1)).pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

        verify(replicationAgentManager, times(1)).createPreviewReplicationAgent(instanceId, PreviewPublishAemBaseUrl,
            authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        verify(replicationAgentManager, times(1)).pausePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

        verify(replicationAgentManager, times(1)).resumePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        assertThat(result, equalTo(false));
    }

    @Test
    public void testFailedToResumeActivePreviewPublishReplicationAgent() throws Exception {
        doThrow(new ApiException("Test")).when(replicationAgentManager).resumePreviewReplicationAgent(activePreviewPublishId,
            authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        boolean result = action.execute(instanceId);

        verify(aemHelperService, times(1)).pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

        verify(replicationAgentManager, times(1)).createPreviewReplicationAgent(instanceId, PreviewPublishAemBaseUrl,
            authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        verify(replicationAgentManager, times(1)).pausePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        verify(aemHelperService, times(1)).tagInstanceWithSnapshotId(instanceId, snapshotId);

        verify(replicationAgentManager, times(1)).resumePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        assertThat(result, equalTo(true));
    }

    @Test
    public void testUnableToFindUnpairedDispatcher() throws Exception {
        when(aemHelperService.findUnpairedPreviewPublishDispatcher(instanceId)).thenThrow(new NoSuchElementException());

        boolean result = action.execute(instanceId);

        verify(aemHelperService, times(0)).pairPreviewPublishWithDispatcher(instanceId, unpairedDispatcherId);

        verify(replicationAgentManager, times(0)).createPreviewReplicationAgent(instanceId, PreviewPublishAemBaseUrl,
            authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        verify(replicationAgentManager, times(0)).pausePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

        verify(replicationAgentManager, times(0)).resumePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        assertThat(result, equalTo(false));
    }

    @Test
    public void testUnablePairPreviewPublishWithDispatcher() throws Exception {
        doThrow(new RuntimeException("Test")).when(aemHelperService).pairPreviewPublishWithDispatcher(instanceId,
            unpairedDispatcherId);

        boolean result = action.execute(instanceId);

        verify(replicationAgentManager, times(0)).createPreviewReplicationAgent(instanceId, PreviewPublishAemBaseUrl,
            authorAemBaseUrl, AgentRunMode.PREVIEWPUBLISH);

        verify(replicationAgentManager, times(0)).pausePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        verify(aemHelperService, times(0)).tagInstanceWithSnapshotId(instanceId, snapshotId);

        verify(replicationAgentManager, times(0)).resumePreviewReplicationAgent(activePreviewPublishId, authorAemBaseUrl,
            AgentRunMode.PREVIEWPUBLISH);

        assertThat(result, equalTo(false));
    }


    private void enableReverseReplication() {
        setField(action, "enableReverseReplication", true);
    }

}
