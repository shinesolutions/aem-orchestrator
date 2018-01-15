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
public class FlushAgentManagerTest {

    private AemApiFactory aemApiFactory;

    private AemApiHelper aemApiHelper;

    private AgentRequestFactory agentRequestFactory;

    @InjectMocks
    private FlushAgentManager flushAgentManager;

    @Before
    public void setup() {
        setupAemApiFactory();
        aemApiHelper = spy(new AemApiHelper());
        agentRequestFactory = spy(new AgentRequestFactory());

        setField(flushAgentManager, "aemApiFactory", aemApiFactory);
        setField(flushAgentManager, "agentRequestFactory", agentRequestFactory);
        setField(flushAgentManager, "aemApiHelper", aemApiHelper);
    }

    private void setupAemApiFactory() {
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
    }

    @Test
    public void testCreateFlushAgent() throws ApiException {
        ApiResponse<Void> response = new ApiResponse<>(1, null, null);
        doReturn(response).when(aemApiHelper).postAgentWithHttpInfo(any(SlingApi.class), any(PostAgentWithHttpInfoRequest.class));

        final String instanceId = "testInstanceId";
        final String aemBaseUrl = "testAemBaseUrl";
        final String aemDispatcherBaseUrl = "testAemDispatcherBaseUrl";
        final AgentRunMode runMode = AgentRunMode.AUTHOR;
        flushAgentManager.createFlushAgent(instanceId, aemBaseUrl, aemDispatcherBaseUrl, runMode);

        verify(agentRequestFactory).getCreateFlushAgentRequest(eq(runMode), endsWith(instanceId), endsWith(instanceId), eq(aemDispatcherBaseUrl));
        verify(aemApiFactory).getSlingApi(eq(aemBaseUrl), eq(AgentAction.CREATE));
        verify(aemApiHelper).postAgentWithHttpInfo(any(SlingApi.class), any(PostAgentWithHttpInfoRequest.class));
    }

    @Test
    public void testDeleteFlushAgent() throws ApiException {
        ApiResponse<Void> response = new ApiResponse<>(1, null, null);
        doReturn(response).when(aemApiHelper).postAgentWithHttpInfo(any(SlingApi.class), any(PostAgentWithHttpInfoRequest.class));

        final String instanceId = "testInstanceId";
        final String baseUrl = "testBaseUrl";
        final AgentRunMode runMode = AgentRunMode.AUTHOR;
        flushAgentManager.deleteFlushAgent(instanceId, baseUrl, runMode);

        verify(agentRequestFactory).getDeleteAgentRequest(eq(runMode), endsWith(instanceId));
        verify(aemApiFactory).getSlingApi(eq(baseUrl), eq(AgentAction.DELETE));
        verify(aemApiHelper).postAgentWithHttpInfo(any(SlingApi.class), any(PostAgentWithHttpInfoRequest.class));
    }
}
