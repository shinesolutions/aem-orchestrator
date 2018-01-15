package com.shinesolutions.aemorchestrator.aem;

import com.shinesolutions.aemorchestrator.model.AemCredentials;
import com.shinesolutions.aemorchestrator.model.UserPasswordCredentials;
import com.shinesolutions.swaggeraem4j.api.SlingApi;
import com.shinesolutions.swaggeraem4j.auth.HttpBasicAuth;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class AemApiFactoryTest {

    @InjectMocks
    private AemApiFactory aemApiFactory;

    private AemCredentials aemCredentials;

    private String basePath;

    private Integer connectionTimeout;

    private Boolean useDebug;

    @Before
    public void setup() {
        basePath = "testBasePath";
        useDebug = false;
        connectionTimeout = 30000;

        setField(aemApiFactory, "useDebug", useDebug);
        setField(aemApiFactory, "connectionTimeout", connectionTimeout);

        UserPasswordCredentials replicatorCredentials = new UserPasswordCredentials();
        replicatorCredentials.setUserName("replicatorUsername");
        replicatorCredentials.setPassword("replicatorPassword");

        UserPasswordCredentials orchestratorCredentials = new UserPasswordCredentials();
        replicatorCredentials.setUserName("orchestratorUsername");
        replicatorCredentials.setPassword("orchestratorPassword");

        aemCredentials = new AemCredentials();
        aemCredentials.setReplicatorCredentials(replicatorCredentials);
        aemCredentials.setOrchestratorCredentials(orchestratorCredentials);

        setField(aemApiFactory, "aemCredentials", aemCredentials);
    }

    @Test
    public void testGetSlingApi_ClientIsOrchestrator() {
        SlingApi result = aemApiFactory.getSlingApi(basePath, AgentAction.DELETE);

        assertThat(result.getApiClient().getBasePath(), equalTo(basePath));
        assertThat(result.getApiClient().isDebugging(), is(useDebug));
        assertThat(result.getApiClient().getConnectTimeout(), equalTo(connectionTimeout));

        HttpBasicAuth auth = (HttpBasicAuth) result.getApiClient().getAuthentication("aemAuth");
        assertThat(auth.getUsername(), equalTo(aemCredentials.getOrchestratorCredentials().getUserName()));
        assertThat(auth.getPassword(), equalTo(aemCredentials.getOrchestratorCredentials().getPassword()));
    }

    @Test
    public void testGetSlingApi_ClientIsReplicator() {
        SlingApi result = aemApiFactory.getSlingApi(basePath, AgentAction.CREATE);

        assertThat(result.getApiClient().getBasePath(), equalTo(basePath));
        assertThat(result.getApiClient().isDebugging(), is(useDebug));
        assertThat(result.getApiClient().getConnectTimeout(), equalTo(connectionTimeout));

        HttpBasicAuth auth = (HttpBasicAuth) result.getApiClient().getAuthentication("aemAuth");
        assertThat(auth.getUsername(), equalTo(aemCredentials.getReplicatorCredentials().getUserName()));
        assertThat(auth.getPassword(), equalTo(aemCredentials.getReplicatorCredentials().getPassword()));
    }
}
