package com.shinesolutions.aemorchestrator.aem;

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
public class FlushAgentManagerTest {

    @Mock
    private AemApiFactory aemApiFactory;

    @Mock
    private AemApiHelper aemApiHelper;

    @Mock
    private AgentRequestFactory agentRequestFactory;

    @InjectMocks
    private FlushAgentManager flushAgentManager;

    private SlingApi slingApi;

    @Before
    public void setup() throws ApiException {
        slingApi = new SlingApi();
        when(aemApiFactory.getSlingApi(anyString(), any(AgentAction.class))).thenReturn(slingApi);

        ApiResponse<Void> response = new ApiResponse<>(1, null, null);
        when(aemApiHelper.postAgentWithHttpInfo(any(SlingApi.class), any(PostAgentWithHttpInfoRequest.class))).thenReturn(response);
    }

    @Test
    public void testCreateFlushAgent() throws ApiException {
        PostAgentWithHttpInfoRequest request = new PostAgentWithHttpInfoRequest();
        when(agentRequestFactory.getCreateFlushAgentRequest(
                any(AgentRunMode.class),
                anyString(),
                anyString(),
                anyString()))
                .thenReturn(request);

        final String instanceId = "testInstanceId";
        final String aemBaseUrl = "testAemBaseUrl";
        final String aemDispatcherBaseUrl = "testAemDispatcherBaseUrl";
        final AgentRunMode runMode = AgentRunMode.AUTHOR;
        flushAgentManager.createFlushAgent(instanceId, aemBaseUrl, aemDispatcherBaseUrl, runMode);

        verify(agentRequestFactory).getCreateFlushAgentRequest(eq(runMode), endsWith(instanceId), endsWith(instanceId), eq(aemDispatcherBaseUrl));
        verify(aemApiFactory).getSlingApi(eq(aemBaseUrl), eq(AgentAction.CREATE));
        verify(aemApiHelper).postAgentWithHttpInfo(slingApi, request);
    }

    @Test
    public void testDeleteFlushAgent() throws ApiException {
        PostAgentWithHttpInfoRequest request = new PostAgentWithHttpInfoRequest();
        when(agentRequestFactory.getDeleteAgentRequest(any(AgentRunMode.class), anyString())).thenReturn(request);

        final String instanceId = "testInstanceId";
        final String baseUrl = "testBaseUrl";
        final AgentRunMode runMode = AgentRunMode.AUTHOR;
        flushAgentManager.deleteFlushAgent(instanceId, baseUrl, runMode);

        verify(agentRequestFactory).getDeleteAgentRequest(eq(runMode), endsWith(instanceId));
        verify(aemApiFactory).getSlingApi(eq(baseUrl), eq(AgentAction.DELETE));
        verify(aemApiHelper).postAgentWithHttpInfo(slingApi, request);
    }
}
