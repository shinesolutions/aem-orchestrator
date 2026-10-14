package com.shinesolutions.aemorchestrator.aem;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.shinesolutions.aemorchestrator.model.AemSsl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgentRequestFactoryTest {

  private static final String DEFAULT_LOG_LEVEL = "info";

  private String aemBaseUrl;

  private String agentDescription;

  private String agentName;

  private AgentRequestFactory agentRequestFactory;

  private AgentRunMode runMode;

  @BeforeEach
  void setup() {
    runMode = AgentRunMode.AUTHOR;
    agentName = "testAgentName";
    agentDescription = "testAgentDescription";
    aemBaseUrl = "testAemBaseUrl";

    agentRequestFactory = new AgentRequestFactory();
  }

  @Test
  void testGetCreateFlushAgentRequest() {
    PostAgentWithHttpInfoRequest request =
        agentRequestFactory.getCreateFlushAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl);
    assertThat(request.getRunMode(), equalTo(runMode.getValue()));
    assertThat(request.getName(), equalTo(agentName));
    assertThat(request.getJcrContentJcrTitle(), equalTo(agentName));
    assertThat(request.getJcrContentJcrDescription(), equalTo(agentDescription));
    assertThat(request.getJcrContentTransportUri(), startsWith(aemBaseUrl));
  }

  @Test
  void testGetCreateFlushAgentRequest_enableRelaxedSsl() {
    setField(agentRequestFactory, "enableRelaxedSsl", true);
    PostAgentWithHttpInfoRequest request =
        agentRequestFactory.getCreateFlushAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl);
    assertThat(request.getJcrContentSsl(), equalTo(AemSsl.RELAXED.getValue()));

    setField(agentRequestFactory, "enableRelaxedSsl", false);
    request =
        agentRequestFactory.getCreateFlushAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl);
    assertThat(request.getJcrContentSsl(), equalTo(AemSsl.DEFAULT.getValue()));
  }

  @Test
  void testGetCreateFlushAgentRequest_flushLogLevel() {
    String logLevel = null;
    setField(agentRequestFactory, "flushLogLevel", logLevel);
    PostAgentWithHttpInfoRequest request =
        agentRequestFactory.getCreateFlushAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl);
    assertThat(request.getJcrContentLogLevel(), equalTo(DEFAULT_LOG_LEVEL));

    logLevel = "";
    setField(agentRequestFactory, "flushLogLevel", logLevel);
    request =
        agentRequestFactory.getCreateFlushAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl);
    assertThat(request.getJcrContentLogLevel(), equalTo(DEFAULT_LOG_LEVEL));

    logLevel = "testLogLevel";
    setField(agentRequestFactory, "flushLogLevel", logLevel);
    request =
        agentRequestFactory.getCreateFlushAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl);
    assertThat(request.getJcrContentLogLevel(), equalTo(logLevel));
  }

  @Test
  void testGetCreateReplicationAgentRequest() {
    String user = "testUser";
    String password = "testPassword";
    PostAgentWithHttpInfoRequest request =
        agentRequestFactory.getCreateReplicationAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl, user, password);
    assertThat(request.getRunMode(), equalTo(runMode.getValue()));
    assertThat(request.getName(), equalTo(agentName));
    assertThat(request.getJcrContentJcrTitle(), equalTo(agentName));
    assertThat(request.getJcrContentJcrDescription(), equalTo(agentDescription));
    assertThat(request.getJcrContentTransportUri(), startsWith(aemBaseUrl));
    assertThat(request.getJcrContentTransportUser(), equalTo(user));
    assertThat(request.getJcrContentTransportPassword(), equalTo(password));
  }

  @Test
  void testGetCreateReplicationAgentRequest_enableRelaxedSsl() {
    setField(agentRequestFactory, "enableRelaxedSsl", true);
    String user = "testUser";
    String password = "testPassword";
    PostAgentWithHttpInfoRequest request =
        agentRequestFactory.getCreateReplicationAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl, user, password);
    assertThat(request.getJcrContentSsl(), equalTo(AemSsl.RELAXED.getValue()));

    setField(agentRequestFactory, "enableRelaxedSsl", false);
    request =
        agentRequestFactory.getCreateReplicationAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl, user, password);
    assertThat(request.getJcrContentSsl(), equalTo(AemSsl.DEFAULT.getValue()));
  }

  @Test
  void testGetCreateReplicationAgentRequest_replicationLogLevel() {
    String logLevel = null;
    setField(agentRequestFactory, "replicationLogLevel", logLevel);
    String user = "testUser";
    String password = "testPassword";
    PostAgentWithHttpInfoRequest request =
        agentRequestFactory.getCreateReplicationAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl, user, password);
    assertThat(request.getJcrContentLogLevel(), equalTo(DEFAULT_LOG_LEVEL));

    logLevel = "";
    setField(agentRequestFactory, "replicationLogLevel", logLevel);
    request =
        agentRequestFactory.getCreateReplicationAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl, user, password);
    assertThat(request.getJcrContentLogLevel(), equalTo(DEFAULT_LOG_LEVEL));

    logLevel = "testLogLevel";
    setField(agentRequestFactory, "replicationLogLevel", logLevel);
    request =
        agentRequestFactory.getCreateReplicationAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl, user, password);
    assertThat(request.getJcrContentLogLevel(), equalTo(logLevel));
  }

  @Test
  void testGetCreateReverseReplicationAgentRequest() {
    String reverseReplicationTransportUriPostfix = "/testUriPostfix";
    setField(
        agentRequestFactory,
        "reverseReplicationTransportUriPostfix",
        reverseReplicationTransportUriPostfix);

    String user = "testUser";
    String password = "testPassword";
    PostAgentWithHttpInfoRequest request =
        agentRequestFactory.getCreateReverseReplicationAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl, user, password);
    assertThat(request.getRunMode(), equalTo(runMode.getValue()));
    assertThat(request.getName(), equalTo(agentName));
    assertThat(request.getJcrContentJcrTitle(), equalTo(agentName));
    assertThat(request.getJcrContentJcrDescription(), equalTo(agentDescription));
    assertThat(
        request.getJcrContentTransportUri(),
        equalTo(aemBaseUrl + reverseReplicationTransportUriPostfix));
    assertThat(request.getJcrContentTransportUser(), equalTo(user));
    assertThat(request.getJcrContentTransportPassword(), equalTo(password));
    assertThat(request.getJcrContentUserId(), equalTo(user));
  }

  @Test
  void testGetCreateReverseReplicationAgentRequest_enableRelaxedSsl() {
    setField(agentRequestFactory, "enableRelaxedSsl", true);

    String user = "testUser";
    String password = "testPassword";
    PostAgentWithHttpInfoRequest request =
        agentRequestFactory.getCreateReverseReplicationAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl, user, password);
    assertThat(request.getJcrContentSsl(), equalTo(AemSsl.RELAXED.getValue()));

    setField(agentRequestFactory, "enableRelaxedSsl", false);
    request =
        agentRequestFactory.getCreateReverseReplicationAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl, user, password);
    assertThat(request.getJcrContentSsl(), equalTo(AemSsl.DEFAULT.getValue()));
  }

  @Test
  void testGetCreateReverseReplicationAgentRequest_replicationLogLevel() {
    String logLevel = null;
    setField(agentRequestFactory, "reverseReplicationLogLevel", logLevel);
    String user = "testUser";
    String password = "testPassword";
    PostAgentWithHttpInfoRequest request =
        agentRequestFactory.getCreateReverseReplicationAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl, user, password);
    assertThat(request.getJcrContentLogLevel(), equalTo(DEFAULT_LOG_LEVEL));

    logLevel = "";
    setField(agentRequestFactory, "reverseReplicationLogLevel", logLevel);
    request =
        agentRequestFactory.getCreateReverseReplicationAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl, user, password);
    assertThat(request.getJcrContentLogLevel(), equalTo(DEFAULT_LOG_LEVEL));

    logLevel = "testLogLevel";
    setField(agentRequestFactory, "reverseReplicationLogLevel", logLevel);
    request =
        agentRequestFactory.getCreateReverseReplicationAgentRequest(
            runMode, agentName, agentDescription, aemBaseUrl, user, password);
    assertThat(request.getJcrContentLogLevel(), equalTo(logLevel));
  }

  @Test
  void testGetDeleteAgentRequest() {
    PostAgentWithHttpInfoRequest request =
        agentRequestFactory.getDeleteAgentRequest(runMode, agentName);

    assertThat(request.getRunMode(), equalTo(runMode.getValue()));
    assertThat(request.getName(), equalTo(agentName));
  }

  @Test
  void testGetPauseReplicationAgentRequest() {
    PostAgentWithHttpInfoRequest request =
        agentRequestFactory.getPauseReplicationAgentRequest(runMode, agentName);

    assertThat(request.getRunMode(), equalTo(runMode.getValue()));
    assertThat(request.getName(), equalTo(agentName));
    assertThat(request.getJcrPrimaryType(), equalTo("cq:Page"));
    assertThat(request.getJcrContentJcrTitle(), equalTo(agentName));
    assertThat(
        request.getJcrContentSlingResourceType(), equalTo("cq/replication/components/agent"));
    assertThat(request.getJcrContentTransportUser(), equalTo("orchestrator-pause"));
    assertThat(request.getJcrContentCqTemplate(), equalTo("/libs/cq/replication/templates/agent"));
    assertThat(request.getJcrContentEnabled(), equalTo(true));

    // Each call builds a new request rather than reusing shared state
    assertThat(
        agentRequestFactory.getPauseReplicationAgentRequest(runMode, agentName),
        not(sameInstance(request)));
  }

  @Test
  void testGetResumeReplicationAgentRequest() {
    String user = "testUser";
    String password = "testPassword";
    PostAgentWithHttpInfoRequest request =
        agentRequestFactory.getResumeReplicationAgentRequest(runMode, agentName, user, password);
    assertThat(request.getRunMode(), equalTo(runMode.getValue()));
    assertThat(request.getName(), equalTo(agentName));
    assertThat(request.getJcrContentJcrTitle(), equalTo(agentName));
    assertThat(request.getJcrContentTransportUser(), equalTo(user));
    assertThat(request.getJcrContentTransportPassword(), equalTo(password));
  }
}
