package com.shinesolutions.aemorchestrator.actions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.shinesolutions.aemorchestrator.aem.AgentRunMode;
import com.shinesolutions.aemorchestrator.aem.ReplicationAgentManager;
import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import com.shinesolutions.swaggeraem4j.ApiException;

@RunWith(MockitoJUnitRunner.class)
public class ScaleDownPreviewPublishActionTest {

    @Mock
    private AemInstanceHelperService aemHelperService;

    @Mock
    private AwsHelperService awsHelperService;

    @Mock
    private ReplicationAgentManager replicationAgentManager;

    @InjectMocks
    private ScaleDownPreviewPublishAction action;

    private String instanceId;
    private String authorAemBaseUrl;
    private String pairedDispatcherId;

    @Before
    public void setUp() throws Exception {
        authorAemBaseUrl = "authorAemBaseUrl";
        instanceId = "instanceId";
        pairedDispatcherId = "pairedDispatcherId";

        when(aemHelperService.getAemUrlForAuthorElb()).thenReturn(authorAemBaseUrl);
    }

    @Test
    public void testTerminatePairedDispatcherAndDeleteReplicationAgent() throws Exception {
        when(aemHelperService.getDispatcherIdForPairedPreviewPublish(instanceId)).thenReturn(pairedDispatcherId);

        boolean success = action.execute(instanceId);

        verify(awsHelperService, times(1)).terminateInstance(pairedDispatcherId);

        verify(replicationAgentManager, times(1)).deleteReplicationAgent(instanceId, authorAemBaseUrl,
            AgentRunMode.AUTHOR);

        //Ensure reverse replication queue removal not called unless enabled
        verify(replicationAgentManager, times(0)).deleteReverseReplicationAgent(instanceId, authorAemBaseUrl,
            AgentRunMode.AUTHOR);

        assertThat(success, equalTo(true));
    }

    @Test
    public void testCantFindPairedDispatcher() throws Exception {
        when(aemHelperService.getDispatcherIdForPairedPreviewPublish(instanceId)).thenReturn(null);

        boolean success = action.execute(instanceId);

        verify(awsHelperService, times(0)).terminateInstance(pairedDispatcherId);

        verify(replicationAgentManager, times(1)).deleteReplicationAgent(instanceId, authorAemBaseUrl,
            AgentRunMode.AUTHOR);

        assertThat(success, equalTo(true));
    }

    @Test
    public void testHandlesExceptionWhenDeletingReplicationAgent() throws Exception {
        when(aemHelperService.getDispatcherIdForPairedPreviewPublish(instanceId)).thenReturn(pairedDispatcherId);

        doThrow(new ApiException()).when(replicationAgentManager).deleteReplicationAgent(instanceId, authorAemBaseUrl,
            AgentRunMode.AUTHOR);

        boolean success = action.execute(instanceId);

        verify(awsHelperService, times(1)).terminateInstance(pairedDispatcherId);

        assertThat(success, equalTo(true));
    }

    @Test
    public void testDeletesReverseReplicationQueueIfEnabled() throws Exception {
        when(aemHelperService.getDispatcherIdForPairedPreviewPublish(instanceId)).thenReturn(pairedDispatcherId);

        setField(action, "reverseReplicationEnabled", true);

        boolean success = action.execute(instanceId);

        verify(awsHelperService, times(1)).terminateInstance(pairedDispatcherId);

        verify(replicationAgentManager, times(1)).deleteReplicationAgent(instanceId, authorAemBaseUrl,
            AgentRunMode.AUTHOR);

        verify(replicationAgentManager, times(1)).deleteReverseReplicationAgent(instanceId, authorAemBaseUrl,
            AgentRunMode.AUTHOR);

        assertThat(success, equalTo(true));
    }

    @Test
    public void testDeletesReverseReplicationWithException() throws Exception {
        when(aemHelperService.getDispatcherIdForPairedPreviewPublish(instanceId)).thenReturn(pairedDispatcherId);

        doThrow(new ApiException()).when(replicationAgentManager).deleteReverseReplicationAgent(instanceId, authorAemBaseUrl,
            AgentRunMode.AUTHOR);

        setField(action, "reverseReplicationEnabled", true);

        boolean success = action.execute(instanceId);

        verify(awsHelperService, times(1)).terminateInstance(pairedDispatcherId);

        verify(replicationAgentManager, times(1)).deleteReplicationAgent(instanceId, authorAemBaseUrl,
            AgentRunMode.AUTHOR);

        assertThat(success, equalTo(true));
    }

}
