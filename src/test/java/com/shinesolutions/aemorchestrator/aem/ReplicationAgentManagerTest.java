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
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class ReplicationAgentManagerTest {

    private AemApiFactory aemApiFactory;

    private AemApiHelper aemApiHelper;

    private AgentRequestFactory agentRequestFactory;

    private String authorAemBaseUrl;

    private String publishAemBaseUrl;

    private String publishId;

    @InjectMocks
    private ReplicationAgentManager replicationAgentManager;

    private AgentRunMode runMode;

    @Before
    public void setup() throws ApiException {
        publishId = "testPublishId";
        publishAemBaseUrl = "testPublishAemBaseUrl";
        authorAemBaseUrl = "testAuthorAemBaseUrl";
        runMode = AgentRunMode.AUTHOR;

        aemApiFactory = spy(new AemApiFactory());

        setField(aemApiFactory, "useDebug", false);
        setField(aemApiFactory, "connectionTimeout", 30000);

        UserPasswordCredentials replicatorCredentials = new UserPasswordCredentials();
        replicatorCredentials.setUserName("replicatorUsername");
        replicatorCredentials.setPassword("replicatorPassword");

        UserPasswordCredentials orchestratorCredentials = new UserPasswordCredentials();
        replicatorCredentials.setUserName("orchestratorUsername");
        replicatorCredentials.setPassword("orchestratorPassword");

        AemCredentials aemCredentials = new AemCredentials();
        aemCredentials.setReplicatorCredentials(replicatorCredentials);
        aemCredentials.setOrchestratorCredentials(orchestratorCredentials);

        setField(aemApiFactory, "aemCredentials", aemCredentials);

        aemApiHelper = spy(new AemApiHelper());
        ApiResponse<Void> response = new ApiResponse<>(1, null, null);
        doReturn(response).when(aemApiHelper).postAgentWithHttpInfo(any(SlingApi.class), any(PostAgentWithHttpInfoRequest.class));

        agentRequestFactory = spy(new AgentRequestFactory());

        setField(replicationAgentManager, "aemCredentials", aemCredentials);
        setField(replicationAgentManager, "aemApiFactory", aemApiFactory);
        setField(replicationAgentManager, "agentRequestFactory", agentRequestFactory);
        setField(replicationAgentManager, "aemApiHelper", aemApiHelper);
    }

    @Test
    public void testCreateReplicationAgent() throws ApiException {
        replicationAgentManager.createReplicationAgent(publishId, publishAemBaseUrl, authorAemBaseUrl, runMode);

        verify(agentRequestFactory).getCreateReplicationAgentRequest(
                eq(runMode),
                endsWith(publishId),
                endsWith(publishId),
                eq(publishAemBaseUrl),
                anyString(),
                anyString());
        verify(aemApiFactory).getSlingApi(eq(authorAemBaseUrl), eq(AgentAction.CREATE));
        verify(aemApiHelper).postAgentWithHttpInfo(any(SlingApi.class), any(PostAgentWithHttpInfoRequest.class));
    }

    @Test
    public void testCreateReverseReplicationAgent() throws ApiException {
        replicationAgentManager.createReverseReplicationAgent(publishId, publishAemBaseUrl, authorAemBaseUrl, runMode);

        verify(agentRequestFactory).getCreateReverseReplicationAgentRequest(
                eq(runMode),
                endsWith(publishId),
                endsWith(publishId),
                eq(publishAemBaseUrl),
                anyString(),
                anyString());
        verify(aemApiFactory).getSlingApi(eq(authorAemBaseUrl), eq(AgentAction.CREATE));
        verify(aemApiHelper).postAgentWithHttpInfo(any(SlingApi.class), any(PostAgentWithHttpInfoRequest.class));
    }

    @Test
    public void testDeleteReplicationAgent() throws ApiException {
        replicationAgentManager.deleteReplicationAgent(publishId, authorAemBaseUrl, runMode);

        verify(agentRequestFactory).getDeleteAgentRequest(eq(runMode), endsWith(publishId));
        verify(aemApiFactory).getSlingApi(eq(authorAemBaseUrl), eq(AgentAction.DELETE));
        verify(aemApiHelper).postAgentWithHttpInfo(any(SlingApi.class), any(PostAgentWithHttpInfoRequest.class));
    }

    @Test
    public void testDeleteReverseReplicationAgent() throws ApiException {
        replicationAgentManager.deleteReverseReplicationAgent(publishId, authorAemBaseUrl, runMode);

        verify(agentRequestFactory).getDeleteAgentRequest(eq(runMode), endsWith(publishId));
        verify(aemApiFactory).getSlingApi(eq(authorAemBaseUrl), eq(AgentAction.DELETE));
        verify(aemApiHelper).postAgentWithHttpInfo(any(SlingApi.class), any(PostAgentWithHttpInfoRequest.class));
    }

    @Test
    public void testPauseReplicationAgent() throws ApiException {
        replicationAgentManager.pauseReplicationAgent(publishId, authorAemBaseUrl, runMode);

        verify(agentRequestFactory).getPauseReplicationAgentRequest(eq(runMode), endsWith(publishId));
        verify(aemApiFactory).getSlingApi(eq(authorAemBaseUrl), eq(AgentAction.PAUSE));
        verify(aemApiHelper).postAgentWithHttpInfo(any(SlingApi.class), any(PostAgentWithHttpInfoRequest.class));
    }

    @Test
    public void testResumeReplicationAgent() throws ApiException {
        replicationAgentManager.resumeReplicationAgent(publishId, authorAemBaseUrl, runMode);

        verify(agentRequestFactory).getResumeReplicationAgentRequest(
                eq(runMode),
                endsWith(publishId),
                anyString(),
                anyString());
        verify(aemApiFactory).getSlingApi(eq(authorAemBaseUrl), eq(AgentAction.RESTART));
        verify(aemApiHelper).postAgentWithHttpInfo(any(SlingApi.class), any(PostAgentWithHttpInfoRequest.class));
    }
}
