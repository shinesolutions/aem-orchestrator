package com.shinesolutions.aemorchestrator.aem;

import static com.shinesolutions.aemorchestrator.model.AemSsl.DEFAULT;
import static com.shinesolutions.aemorchestrator.model.AemSsl.RELAXED;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// TODO: modify all methods here to use this.request
// to allow the injection of a mock PostAgentWithHttpInfoRequest

/**
 * Builds {@link PostAgentWithHttpInfoRequest}s for creating, pausing, resuming and deleting AEM
 * flush and replication agents.
 *
 * <p>Agent log levels, relaxed SSL and the reverse replication transport URI postfix come from the
 * {@code aem.*} application properties.
 */
@Component
public class AgentRequestFactory {

  @Value("${aem.reverseReplication.transportUri.postfix}")
  private String reverseReplicationTransportUriPostfix;

  @Value("${aem.relaxed.ssl.enable}")
  private boolean enableRelaxedSsl;

  @Value("${aem.flush.logLevel}")
  private String flushLogLevel;

  @Value("${aem.replication.logLevel}")
  private String replicationLogLevel;

  @Value("${aem.reverseReplication.logLevel}")
  private String reverseReplicationLogLevel;

  private static final String JCR_PRIMARY_TYPE = "cq:Page";
  private static final String SLING_RESOURCE_TYPE_REPLICATION_AGENT =
      "cq/replication/components/agent";
  private static final String CQ_TEMPLATE_REPLICATION_AGENT =
      "/libs/cq/replication/templates/agent";
  private static final String SLING_RESOURCE_TYPE_REVERSE_REPLICATION_AGENT =
      "cq/replication/components/revagent";
  private static final String CQ_TEMPLATE_REVERSE_REPLICATION_AGENT =
      "/libs/cq/replication/templates/revagent";
  private static final String TRANSPORT_URI_POSTFIX = "/bin/receive?sling:authRequestLogin=1";
  private static final String DEFAULT_LOG_LEVEL = "info";
  private static final String OPERATION_DELETE = "delete";

  private PostAgentWithHttpInfoRequest request;

  /** Creates a factory whose pause requests are built on a fresh request object. */
  public AgentRequestFactory() {
    this.request = new PostAgentWithHttpInfoRequest();
  }

  /**
   * Creates a factory whose pause requests are built on the given request object, allowing tests to
   * inject a mock.
   */
  public AgentRequestFactory(PostAgentWithHttpInfoRequest request) {
    this.request = request;
  }

  /**
   * Builds a request that creates an enabled flush agent which invalidates the dispatcher cache.
   *
   * <p>The agent sends {@code GET <aemDispatcherBaseUrl>/dispatcher/invalidate.cache} with a {@code
   * CQ-Action} header on receive and on specific (explicit) triggers, using flush serialization and
   * no transport credentials.
   *
   * @param agentName agent node name, also used as its title
   * @param aemDispatcherBaseUrl base URL of the dispatcher whose cache the agent flushes
   */
  public PostAgentWithHttpInfoRequest getCreateFlushAgentRequest(
      AgentRunMode runMode,
      String agentName,
      String agentDescription,
      String aemDispatcherBaseUrl) {

    return new PostAgentWithHttpInfoRequest()
        .withRunMode(runMode.getValue())
        .withName(agentName)
        .withJcrPrimaryType(JCR_PRIMARY_TYPE)
        .withJcrContentJcrTitle(agentName)
        .withJcrContentJcrDescription(agentDescription)
        .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE_REPLICATION_AGENT)
        .withJcrContentTransportUri(aemDispatcherBaseUrl + "/dispatcher/invalidate.cache")
        .withJcrContentTransportUser("")
        .withJcrContentTransportPassword("")
        .withJcrContentLogLevel(getLogType(flushLogLevel))
        .withJcrContentNoVersioning(true)
        .withJcrContentProtocolHttpHeaders(Arrays.asList("CQ-Action:{action}"))
        .withJcrContentProtocolHttpHeadersTypeHint("String[]")
        .withJcrContentProtocolHttpMethod("GET")
        .withJcrContentRetryDelay("" + TimeUnit.MINUTES.toMillis(1))
        .withJcrContentSerializationType("flush")
        .withJcrContentJcrMixinTypes("cq:ReplicationStatus")
        .withJcrContentTriggerReceive(true)
        .withJcrContentTriggerSpecific(true)
        .withJcrContentCqTemplate(CQ_TEMPLATE_REPLICATION_AGENT)
        .withJcrContentSsl(enableRelaxedSsl ? RELAXED.getValue() : DEFAULT.getValue())
        .withJcrContentEnabled(true);
  }

  /**
   * Builds a request that creates an enabled replication agent which pushes content to the AEM
   * instance at {@code aemBaseUrl} via its {@code /bin/receive} endpoint using durbo serialization.
   *
   * @param agentName agent node name, also used as its title
   * @param aemBaseUrl base URL of the target (publish) instance
   * @param user transport user on the target instance
   * @param password transport password on the target instance
   */
  public PostAgentWithHttpInfoRequest getCreateReplicationAgentRequest(
      AgentRunMode runMode,
      String agentName,
      String agentDescription,
      String aemBaseUrl,
      String user,
      String password) {

    return new PostAgentWithHttpInfoRequest()
        .withRunMode(runMode.getValue())
        .withName(agentName)
        .withJcrPrimaryType(JCR_PRIMARY_TYPE)
        .withJcrContentJcrTitle(agentName)
        .withJcrContentJcrDescription(agentDescription)
        .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE_REPLICATION_AGENT)
        .withJcrContentTransportUri(aemBaseUrl + TRANSPORT_URI_POSTFIX)
        .withJcrContentTransportUser(user)
        .withJcrContentTransportPassword(password)
        .withJcrContentLogLevel(getLogType(replicationLogLevel))
        .withJcrContentRetryDelay("" + TimeUnit.MINUTES.toMillis(1))
        .withJcrContentSerializationType("durbo")
        .withJcrContentCqTemplate(CQ_TEMPLATE_REPLICATION_AGENT)
        .withJcrContentSsl(enableRelaxedSsl ? RELAXED.getValue() : DEFAULT.getValue())
        .withJcrContentEnabled(true);
  }

  /**
   * Builds a request that creates an enabled reverse replication agent which pulls content from the
   * outbox of the AEM instance at {@code aemBaseUrl}.
   *
   * <p>The transport URI is {@code aemBaseUrl} plus the configured {@code
   * aem.reverseReplication.transportUri.postfix}; {@code user} is used both for transport and as
   * the agent's user id.
   *
   * @param agentName agent node name, also used as its title
   * @param aemBaseUrl base URL of the source (publish) instance
   * @param user transport user on the source instance
   * @param password transport password on the source instance
   */
  public PostAgentWithHttpInfoRequest getCreateReverseReplicationAgentRequest(
      AgentRunMode runMode,
      String agentName,
      String agentDescription,
      String aemBaseUrl,
      String user,
      String password) {

    return new PostAgentWithHttpInfoRequest()
        .withRunMode(runMode.getValue())
        .withName(agentName)
        .withJcrPrimaryType(JCR_PRIMARY_TYPE)
        .withJcrContentJcrTitle(agentName)
        .withJcrContentJcrDescription(agentDescription)
        .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE_REVERSE_REPLICATION_AGENT)
        .withJcrContentTransportUri(aemBaseUrl + reverseReplicationTransportUriPostfix)
        .withJcrContentTransportUser(user)
        .withJcrContentTransportPassword(password)
        .withJcrContentUserId(user)
        .withJcrContentLogLevel(getLogType(reverseReplicationLogLevel))
        .withJcrContentProtocolHttpHeaders(Collections.emptyList())
        .withJcrContentProtocolHttpHeadersTypeHint(null)
        .withJcrContentProtocolHttpMethod("GET")
        .withJcrContentRetryDelay("" + TimeUnit.MINUTES.toMillis(1))
        .withJcrContentSerializationType("durbo")
        .withJcrContentCqTemplate(CQ_TEMPLATE_REVERSE_REPLICATION_AGENT)
        .withJcrContentReverseReplication(true)
        .withJcrContentSsl(enableRelaxedSsl ? RELAXED.getValue() : DEFAULT.getValue())
        .withJcrContentEnabled(true);
  }

  /**
   * Builds a request that effectively pauses a replication agent by setting its transport user to
   * an invalid user, so replication to the target fails and content queues until it is resumed.
   *
   * <p>Unlike the other methods, this populates and returns the factory's shared request object
   * rather than a new one.
   */
  public PostAgentWithHttpInfoRequest getPauseReplicationAgentRequest(
      AgentRunMode runMode, String agentName) {

    return this.request
        .withRunMode(runMode.getValue())
        .withName(agentName)
        .withJcrPrimaryType(JCR_PRIMARY_TYPE)
        .withJcrContentJcrTitle(agentName)
        .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE_REPLICATION_AGENT)
        .withJcrContentTransportUser("orchestrator-pause") // Is meant be an invalid user
        .withJcrContentCqTemplate(CQ_TEMPLATE_REPLICATION_AGENT)
        .withJcrContentEnabled(true);
  }

  /**
   * Builds a request that resumes a replication agent paused by {@link
   * #getPauseReplicationAgentRequest} by restoring valid transport credentials.
   */
  public PostAgentWithHttpInfoRequest getResumeReplicationAgentRequest(
      AgentRunMode runMode, String agentName, String user, String password) {

    return new PostAgentWithHttpInfoRequest()
        .withRunMode(runMode.getValue())
        .withName(agentName)
        .withJcrPrimaryType(JCR_PRIMARY_TYPE)
        .withJcrContentJcrTitle(agentName)
        .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE_REPLICATION_AGENT)
        .withJcrContentTransportUser(user)
        .withJcrContentTransportPassword(password)
        .withJcrContentCqTemplate(CQ_TEMPLATE_REPLICATION_AGENT)
        .withJcrContentEnabled(true);
  }

  /** Builds a request that deletes the named agent of any type via the Sling delete operation. */
  public PostAgentWithHttpInfoRequest getDeleteAgentRequest(
      AgentRunMode runMode, String agentName) {
    return new PostAgentWithHttpInfoRequest()
        .withRunMode(runMode.getValue())
        .withName(agentName)
        .withOperation(OPERATION_DELETE);
  }

  private String getLogType(String agentLogLevel) {
    return (agentLogLevel != null && !agentLogLevel.isEmpty()) ? agentLogLevel : DEFAULT_LOG_LEVEL;
  }
}
