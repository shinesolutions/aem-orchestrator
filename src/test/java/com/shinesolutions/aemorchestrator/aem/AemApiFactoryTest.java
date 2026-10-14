package com.shinesolutions.aemorchestrator.aem;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.shinesolutions.aemorchestrator.model.AemCredentials;
import com.shinesolutions.aemorchestrator.model.UserPasswordCredentials;
import com.shinesolutions.swaggeraem4j.api.SlingApi;
import com.shinesolutions.swaggeraem4j.auth.HttpBasicAuth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AemApiFactoryTest {

  @InjectMocks private AemApiFactory aemApiFactory;

  @Mock private AemCredentials aemCredentials;

  private String basePath;

  private Integer connectionTimeout;

  private Boolean useDebug;

  private Boolean verifySsl;

  @BeforeEach
  void setup() {
    basePath = "testBasePath";
    useDebug = false;
    verifySsl = false;
    connectionTimeout = 30000;

    setField(aemApiFactory, "useDebug", useDebug);
    setField(aemApiFactory, "connectionTimeout", connectionTimeout);
    setField(aemApiFactory, "verifySsl", verifySsl);
  }

  @Test
  void testGetSlingApi_clientIsOrchestrator() {
    UserPasswordCredentials orchestratorCredentials =
        new UserPasswordCredentials("orchestratorUsername", "orchestratorPassword");
    when(aemCredentials.getOrchestratorCredentials()).thenReturn(orchestratorCredentials);

    SlingApi result = aemApiFactory.getSlingApi(basePath, AgentAction.DELETE);

    assertThat(result.getApiClient().getBasePath(), equalTo(basePath));
    assertThat(result.getApiClient().isDebugging(), is(useDebug));
    assertThat(result.getApiClient().isVerifyingSsl(), is(verifySsl));
    assertThat(result.getApiClient().getConnectTimeout(), equalTo(connectionTimeout));

    HttpBasicAuth auth = (HttpBasicAuth) result.getApiClient().getAuthentication("aemAuth");
    assertThat(auth.getUsername(), equalTo(orchestratorCredentials.getUserName()));
    assertThat(auth.getPassword(), equalTo(orchestratorCredentials.getPassword()));
  }

  @Test
  void testGetSlingApi_clientIsReplicator() {
    UserPasswordCredentials replicatorCredentials =
        new UserPasswordCredentials("replicatorUsername", "replicatorPassword");
    when(aemCredentials.getReplicatorCredentials()).thenReturn(replicatorCredentials);

    SlingApi result = aemApiFactory.getSlingApi(basePath, AgentAction.CREATE);

    assertThat(result.getApiClient().getBasePath(), equalTo(basePath));
    assertThat(result.getApiClient().isDebugging(), is(useDebug));
    assertThat(result.getApiClient().isVerifyingSsl(), is(verifySsl));
    assertThat(result.getApiClient().getConnectTimeout(), equalTo(connectionTimeout));

    HttpBasicAuth auth = (HttpBasicAuth) result.getApiClient().getAuthentication("aemAuth");
    assertThat(auth.getUsername(), equalTo(replicatorCredentials.getUserName()));
    assertThat(auth.getPassword(), equalTo(replicatorCredentials.getPassword()));
  }
}
