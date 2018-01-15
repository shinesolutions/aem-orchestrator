package com.shinesolutions.aemorchestrator.aem;

import com.shinesolutions.aemorchestrator.model.AemSSL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class AgentRequestFactoryTest {

    private static final String DEFAULT_LOG_LEVEL = "info";

    private String aemBaseUrl;

    private String agentDescription;

    private String agentName;

    private AgentRequestFactory agentRequestFactory;

    private AgentRunMode runMode;

    @Before
    public void setup() {
        runMode = AgentRunMode.AUTHOR;
        agentName = "testAgentName";
        agentDescription = "testAgentDescription";
        aemBaseUrl = "testAemBaseUrl";

        agentRequestFactory = new AgentRequestFactory();
    }

    @Test
    public void testGetCreateFlushAgentRequest() {
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getCreateFlushAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl);
        assertThat(request.getRunMode(), equalTo(runMode.getValue()));
        assertThat(request.getName(), equalTo(agentName));
        assertThat(request.getJcrContentJcrTitle(), equalTo(agentName));
        assertThat(request.getJcrContentJcrDescription(), equalTo(agentDescription));
        assertThat(request.getJcrContentTransportUri(), startsWith(aemBaseUrl));
    }

    @Test
    public void testGetCreateFlushAgentRequest_EnableRelaxedSsl() {
        setField(agentRequestFactory, "enableRelaxedSSL", true);
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getCreateFlushAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl);
        assertThat(request.getJcrContentSSL(), equalTo(AemSSL.RELAXED.getValue()));

        setField(agentRequestFactory, "enableRelaxedSSL", false);
        request = agentRequestFactory.getCreateFlushAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl);
        assertThat(request.getJcrContentSSL(), equalTo(AemSSL.DEFAULT.getValue()));
    }

    @Test
    public void testGetCreateFlushAgentRequest_FlushLogLevel() {
        String logLevel = null;
        setField(agentRequestFactory, "flushLogLevel", logLevel);
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getCreateFlushAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl);
        assertThat(request.getJcrContentLogLevel(), equalTo(DEFAULT_LOG_LEVEL));

        logLevel = "";
        setField(agentRequestFactory, "flushLogLevel", logLevel);
        request = agentRequestFactory.getCreateFlushAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl);
        assertThat(request.getJcrContentLogLevel(), equalTo(DEFAULT_LOG_LEVEL));

        logLevel = "testLogLevel";
        setField(agentRequestFactory, "flushLogLevel", logLevel);
        request = agentRequestFactory.getCreateFlushAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl);
        assertThat(request.getJcrContentLogLevel(), equalTo(logLevel));
    }

    @Test
    public void testGetCreateReplicationAgentRequest() {
        String user = "testUser";
        String password = "testPassword";
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getCreateReplicationAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl,
                user,
                password);
        assertThat(request.getRunMode(), equalTo(runMode.getValue()));
        assertThat(request.getName(), equalTo(agentName));
        assertThat(request.getJcrContentJcrTitle(), equalTo(agentName));
        assertThat(request.getJcrContentJcrDescription(), equalTo(agentDescription));
        assertThat(request.getJcrContentTransportUri(), startsWith(aemBaseUrl));
        assertThat(request.getJcrContentTransportUser(), equalTo(user));
        assertThat(request.getJcrContentTransportPassword(), equalTo(password));
    }

    @Test
    public void testGetCreateReplicationAgentRequest_EnableRelaxedSsl() {
        setField(agentRequestFactory, "enableRelaxedSSL", true);
        String user = "testUser";
        String password = "testPassword";
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getCreateReplicationAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl,
                user,
                password);
        assertThat(request.getJcrContentSSL(), equalTo(AemSSL.RELAXED.getValue()));

        setField(agentRequestFactory, "enableRelaxedSSL", false);
        request = agentRequestFactory.getCreateReplicationAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl,
                user,
                password);
        assertThat(request.getJcrContentSSL(), equalTo(AemSSL.DEFAULT.getValue()));
    }

    @Test
    public void testGetCreateReplicationAgentRequest_ReplicationLogLevel() {
        String logLevel = null;
        setField(agentRequestFactory, "replicationLogLevel", logLevel);
        String user = "testUser";
        String password = "testPassword";
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getCreateReplicationAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl,
                user,
                password);
        assertThat(request.getJcrContentLogLevel(), equalTo(DEFAULT_LOG_LEVEL));

        logLevel = "";
        setField(agentRequestFactory, "replicationLogLevel", logLevel);
        request = agentRequestFactory.getCreateReplicationAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl,
                user,
                password);
        assertThat(request.getJcrContentLogLevel(), equalTo(DEFAULT_LOG_LEVEL));

        logLevel = "testLogLevel";
        setField(agentRequestFactory, "replicationLogLevel", logLevel);
        request = agentRequestFactory.getCreateReplicationAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl,
                user,
                password);
        assertThat(request.getJcrContentLogLevel(), equalTo(logLevel));
    }

    @Test
    public void testGetCreateReverseReplicationAgentRequest() {
        String reverseReplicationTransportUriPostfix = "/testUriPostfix";
        setField(agentRequestFactory, "reverseReplicationTransportUriPostfix", reverseReplicationTransportUriPostfix);

        String user = "testUser";
        String password = "testPassword";
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getCreateReverseReplicationAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl,
                user,
                password);
        assertThat(request.getRunMode(), equalTo(runMode.getValue()));
        assertThat(request.getName(), equalTo(agentName));
        assertThat(request.getJcrContentJcrTitle(), equalTo(agentName));
        assertThat(request.getJcrContentJcrDescription(), equalTo(agentDescription));
        assertThat(request.getJcrContentTransportUri(), equalTo(aemBaseUrl + reverseReplicationTransportUriPostfix));
        assertThat(request.getJcrContentTransportUser(), equalTo(user));
        assertThat(request.getJcrContentTransportPassword(), equalTo(password));
        assertThat(request.getJcrContentUserId(), equalTo(user));
    }

    @Test
    public void testGetCreateReverseReplicationAgentRequest_EnableRelaxedSsl() {
        setField(agentRequestFactory, "enableRelaxedSSL", true);

        String user = "testUser";
        String password = "testPassword";
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getCreateReverseReplicationAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl,
                user,
                password);
        assertThat(request.getJcrContentSSL(), equalTo(AemSSL.RELAXED.getValue()));

        setField(agentRequestFactory, "enableRelaxedSSL", false);
        request = agentRequestFactory.getCreateReverseReplicationAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl,
                user,
                password);
        assertThat(request.getJcrContentSSL(), equalTo(AemSSL.DEFAULT.getValue()));
    }

    @Test
    public void testGetCreateReverseReplicationAgentRequest_ReplicationLogLevel() {
        String logLevel = null;
        setField(agentRequestFactory, "reverseReplicationLogLevel", logLevel);
        String user = "testUser";
        String password = "testPassword";
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getCreateReverseReplicationAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl,
                user,
                password);
        assertThat(request.getJcrContentLogLevel(), equalTo(DEFAULT_LOG_LEVEL));

        logLevel = "";
        setField(agentRequestFactory, "reverseReplicationLogLevel", logLevel);
        request = agentRequestFactory.getCreateReverseReplicationAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl,
                user,
                password);
        assertThat(request.getJcrContentLogLevel(), equalTo(DEFAULT_LOG_LEVEL));

        logLevel = "testLogLevel";
        setField(agentRequestFactory, "reverseReplicationLogLevel", logLevel);
        request = agentRequestFactory.getCreateReverseReplicationAgentRequest(
                runMode,
                agentName,
                agentDescription,
                aemBaseUrl,
                user,
                password);
        assertThat(request.getJcrContentLogLevel(), equalTo(logLevel));
    }

    @Test
    public void testGetDeleteAgentRequest() {
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getDeleteAgentRequest(runMode, agentName);

        assertThat(request.getRunMode(), equalTo(runMode.getValue()));
        assertThat(request.getName(), equalTo(agentName));
    }

    @Test
    public void testGetPauseReplicationAgentRequest() {

        PostAgentWithHttpInfoRequest request = new PostAgentWithHttpInfoRequest();
        PostAgentWithHttpInfoRequest spy = spy(request);

        when(spy.withRunMode(AgentRunMode.AUTHOR.getValue())).thenReturn(request);
        verify(spy, times(1)).withRunMode(AgentRunMode.AUTHOR.getValue());

        when(spy.withName("some-agent")).thenReturn(request);
        verify(spy, times(1)).withName("some-agent");

        when(spy.withJcrPrimaryType("cq:Page")).thenReturn(request);
        verify(spy, times(1)).withJcrPrimaryType("cq:Page");

        when(spy.withJcrContentJcrTitle("some-agent")).thenReturn(request);
        verify(spy, times(1)).withJcrContentJcrTitle("some-agent");

        when(spy.withJcrContentSlingResourceType("cq/replication/components/agent")).thenReturn(request);
        verify(spy, times(1)).withJcrContentSlingResourceType("cq/replication/components/agent");

        when(spy.withJcrContentTransportUser("orchestrator-pause")).thenReturn(request);
        verify(spy, times(1)).withJcrContentTransportUser("orchestrator-pause");

        when(spy.withJcrContentCqTemplate("/libs/cq/replication/templates/agent")).thenReturn(request);
        verify(spy, times(1)).withJcrContentCqTemplate("/libs/cq/replication/templates/agent");

        when(spy.withJcrContentEnabled(true)).thenReturn(request);
        verify(spy, times(1)).withJcrContentEnabled(true);

        AgentRequestFactory factory = new AgentRequestFactory(request);
        factory.getPauseReplicationAgentRequest(AgentRunMode.AUTHOR, "some-agent");
    }

    @Test
    public void testGetResumeReplicationAgentRequest() {
        String user = "testUser";
        String password = "testPassword";
        PostAgentWithHttpInfoRequest request = agentRequestFactory.getResumeReplicationAgentRequest(
                runMode,
                agentName,
                user,
                password);
        assertThat(request.getRunMode(), equalTo(runMode.getValue()));
        assertThat(request.getName(), equalTo(agentName));
        assertThat(request.getJcrContentJcrTitle(), equalTo(agentName));
        assertThat(request.getJcrContentTransportUser(), equalTo(user));
        assertThat(request.getJcrContentTransportPassword(), equalTo(password));
    }
}
