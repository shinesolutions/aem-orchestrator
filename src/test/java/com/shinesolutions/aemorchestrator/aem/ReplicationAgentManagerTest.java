package com.shinesolutions.aemorchestrator.aem;

import com.shinesolutions.aemorchestrator.model.AemCredentials;
import com.shinesolutions.aemorchestrator.model.UserPasswordCredentials;
import com.shinesolutions.swaggeraem4j.ApiException;
import com.shinesolutions.swaggeraem4j.ApiResponse;
import com.shinesolutions.swaggeraem4j.api.SlingApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReplicationAgentManagerTest {

    @Mock
    private AemApiFactory aemApiFactory;

    @Mock
    private AemApiHelper aemApiHelper;

    @Mock
    private AemCredentials aemCredentials;

    @Mock
    private AgentRequestFactory agentRequestFactory;

    private String authorAemBaseUrl;

    private String password;

    private String publishAemBaseUrl;

    private String publishId;

    @InjectMocks
    private ReplicationAgentManager replicationAgentManager;

    private SlingApi slingApi;

    private AgentRunMode runMode;

    private String username;


    @Before
    public void setup() throws ApiException {
        publishId = "testPublishId";
        publishAemBaseUrl = "testPublishAemBaseUrl";
        authorAemBaseUrl = "testAuthorAemBaseUrl";
        runMode = AgentRunMode.AUTHOR;
        username = "replicatorUsername";
        password = "replicatorPassword";

        UserPasswordCredentials replicatorCredentials = new UserPasswordCredentials();
        replicatorCredentials.setUserName(username);
        replicatorCredentials.setPassword(password);
        when(aemCredentials.getReplicatorCredentials()).thenReturn(replicatorCredentials);

        slingApi = new SlingApi();
        when(aemApiFactory.getSlingApi(anyString(), any(AgentAction.class))).thenReturn(slingApi);

        ApiResponse<Void> response = new ApiResponse<>(1, null, null);
        when(aemApiHelper.postAgentWithHttpInfo(any(SlingApi.class), any(PostAgentWithHttpInfoRequest.class))).thenReturn(response);
    }

    @Test
    public void testCreateReplicationAgent() throws ApiException {
        PostAgentWithHttpInfoRequest request = new PostAgentWithHttpInfoRequest();
        when(agentRequestFactory.getCreateReplicationAgentRequest(
                any(AgentRunMode.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()))
                .thenReturn(request);

        replicationAgentManager.createReplicationAgent(publishId, publishAemBaseUrl, authorAemBaseUrl, runMode);

        verify(agentRequestFactory).getCreateReplicationAgentRequest(
                eq(runMode),
                endsWith(publishId),
                endsWith(publishId),
                eq(publishAemBaseUrl),
                eq(username),
                eq(password));
        verify(aemApiFactory).getSlingApi(eq(authorAemBaseUrl), eq(AgentAction.CREATE));
        verify(aemApiHelper).postAgentWithHttpInfo(slingApi, request);
    }

    @Test
    public void testCreateReverseReplicationAgent() throws ApiException {
        PostAgentWithHttpInfoRequest request = new PostAgentWithHttpInfoRequest();
        when(agentRequestFactory.getCreateReverseReplicationAgentRequest(
                any(AgentRunMode.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()))
                .thenReturn(request);

        replicationAgentManager.createReverseReplicationAgent(publishId, publishAemBaseUrl, authorAemBaseUrl, runMode);

        verify(agentRequestFactory).getCreateReverseReplicationAgentRequest(
                eq(runMode),
                endsWith(publishId),
                endsWith(publishId),
                eq(publishAemBaseUrl),
                eq(username),
                eq(password));
        verify(aemApiFactory).getSlingApi(eq(authorAemBaseUrl), eq(AgentAction.CREATE));
        verify(aemApiHelper).postAgentWithHttpInfo(slingApi, request);
    }

    @Test
    public void testDeleteReplicationAgent() throws ApiException {
        PostAgentWithHttpInfoRequest request = new PostAgentWithHttpInfoRequest();
        when(agentRequestFactory.getDeleteAgentRequest(any(AgentRunMode.class), anyString())).thenReturn(request);

        replicationAgentManager.deleteReplicationAgent(publishId, authorAemBaseUrl, runMode);

        verify(agentRequestFactory).getDeleteAgentRequest(eq(runMode), endsWith(publishId));
        verify(aemApiFactory).getSlingApi(eq(authorAemBaseUrl), eq(AgentAction.DELETE));
        verify(aemApiHelper).postAgentWithHttpInfo(slingApi, request);
    }

    @Test
    public void testDeleteReverseReplicationAgent() throws ApiException {
        PostAgentWithHttpInfoRequest request = new PostAgentWithHttpInfoRequest();
        when(agentRequestFactory.getDeleteAgentRequest(any(AgentRunMode.class), anyString())).thenReturn(request);

        replicationAgentManager.deleteReverseReplicationAgent(publishId, authorAemBaseUrl, runMode);

        verify(agentRequestFactory).getDeleteAgentRequest(eq(runMode), endsWith(publishId));
        verify(aemApiFactory).getSlingApi(eq(authorAemBaseUrl), eq(AgentAction.DELETE));
        verify(aemApiHelper).postAgentWithHttpInfo(slingApi, request);
    }

    @Test
    public void testPauseReplicationAgent() throws ApiException {
        PostAgentWithHttpInfoRequest request = new PostAgentWithHttpInfoRequest();
        when(agentRequestFactory.getPauseReplicationAgentRequest(any(AgentRunMode.class), anyString())).thenReturn(request);

        replicationAgentManager.pauseReplicationAgent(publishId, authorAemBaseUrl, runMode);

        verify(agentRequestFactory).getPauseReplicationAgentRequest(eq(runMode), endsWith(publishId));
        verify(aemApiFactory).getSlingApi(eq(authorAemBaseUrl), eq(AgentAction.PAUSE));
        verify(aemApiHelper).postAgentWithHttpInfo(slingApi, request);
    }

    @Test
    public void testResumeReplicationAgent() throws ApiException {
        PostAgentWithHttpInfoRequest request = new PostAgentWithHttpInfoRequest();
        when(agentRequestFactory.getResumeReplicationAgentRequest(
                any(AgentRunMode.class),
                anyString(),
                anyString(),
                anyString()))
                .thenReturn(request);

        replicationAgentManager.resumeReplicationAgent(publishId, authorAemBaseUrl, runMode);

        verify(agentRequestFactory).getResumeReplicationAgentRequest(
                eq(runMode),
                endsWith(publishId),
                eq(username),
                eq(password));
        verify(aemApiFactory).getSlingApi(eq(authorAemBaseUrl), eq(AgentAction.RESTART));
        verify(aemApiHelper).postAgentWithHttpInfo(slingApi, request);
    }
}
