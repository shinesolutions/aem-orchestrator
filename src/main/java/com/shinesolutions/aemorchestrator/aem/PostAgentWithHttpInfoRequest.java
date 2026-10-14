package com.shinesolutions.aemorchestrator.aem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parameter object for the swaggeraem4j {@code SlingApi.postAgentWithHttpInfo} call, which takes
 * over fifty positional arguments.
 *
 * <p>Each property maps to one request parameter posted to {@code
 * /etc/replication/agents.<runMode>/<name>}, mostly properties of the agent's {@code jcr:content}
 * node. Unset properties are passed as {@code null} and omitted from the request. The {@code with*}
 * methods set a property and return this instance for chaining; {@link AemApiHelper} unpacks the
 * object into the API call.
 */
public class PostAgentWithHttpInfoRequest {

  private String runMode;
  private String name;
  private Boolean jcrContentCqDistribute;
  private String jcrContentCqDistributeTypeHint;
  private String jcrContentCqName;
  private String jcrContentCqTemplate;
  private Boolean jcrContentEnabled;
  private String jcrContentJcrDescription;
  private String jcrContentJcrLastModified;
  private String jcrContentJcrLastModifiedBy;
  private String jcrContentJcrMixinTypes;
  private String jcrContentJcrTitle;
  private String jcrContentLogLevel;
  private Boolean jcrContentNoStatusUpdate;
  private Boolean jcrContentNoVersioning;
  private BigDecimal jcrContentProtocolConnectTimeout;
  private Boolean jcrContentProtocolHttpConnectionClosed;
  private String jcrContentProtocolHttpExpired;
  private List<String> jcrContentProtocolHttpHeaders;
  private String jcrContentProtocolHttpHeadersTypeHint;
  private String jcrContentProtocolHttpMethod;
  private Boolean jcrContentProtocolHttpsRelaxed;
  private String jcrContentProtocolInterface;
  private BigDecimal jcrContentProtocolSocketTimeout;
  private String jcrContentProtocolVersion;
  private String jcrContentProxyNtlmDomain;
  private String jcrContentProxyNtlmHost;
  private String jcrContentProxyHost;
  private String jcrContentProxyPassword;
  private BigDecimal jcrContentProxyPort;
  private String jcrContentProxyUser;
  private BigDecimal jcrContentQueueBatchMaxSize;
  private String jcrContentQueueBatchMode;
  private BigDecimal jcrContentQueueBatchWaitTime;
  private String jcrContentRetryDelay;
  private Boolean jcrContentReverseReplication;
  private String jcrContentSerializationType;
  private String jcrContentSlingResourceType;
  private String jcrContentSsl;
  private String jcrContentTransportNtlmDomain;
  private String jcrContentTransportNtlmHost;
  private String jcrContentTransportPassword;
  private String jcrContentTransportUri;
  private String jcrContentTransportUser;
  private Boolean jcrContentTriggerDistribute;
  private Boolean jcrContentTriggerModified;
  private Boolean jcrContentTriggerOnOffTime;
  private Boolean jcrContentTriggerReceive;
  private Boolean jcrContentTriggerSpecific;
  private String jcrContentUserId;
  private String jcrPrimaryType;
  private String operation;

  public String getRunMode() {
    return runMode;
  }

  public void setRunMode(String runMode) {
    this.runMode = runMode;
  }

  /** Sets the run mode selecting the agents folder, {@code /etc/replication/agents.<runMode>}. */
  public PostAgentWithHttpInfoRequest withRunMode(String runMode) {
    this.setRunMode(runMode);
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /** Sets the agent's node name within the run mode's agents folder. */
  public PostAgentWithHttpInfoRequest withName(String name) {
    this.setName(name);
    return this;
  }

  public String getJcrPrimaryType() {
    return jcrPrimaryType;
  }

  public void setJcrPrimaryType(String jcrPrimaryType) {
    this.jcrPrimaryType = jcrPrimaryType;
  }

  /** Sets the agent node's {@code jcr:primaryType} property. */
  public PostAgentWithHttpInfoRequest withJcrPrimaryType(String jcrPrimaryType) {
    this.setJcrPrimaryType(jcrPrimaryType);
    return this;
  }

  public Boolean getJcrContentCqDistribute() {
    return jcrContentCqDistribute;
  }

  public void setJcrContentCqDistribute(Boolean jcrContentCqDistribute) {
    this.jcrContentCqDistribute = jcrContentCqDistribute;
  }

  public String getJcrContentCqDistributeTypeHint() {
    return jcrContentCqDistributeTypeHint;
  }

  public void setJcrContentCqDistributeTypeHint(String jcrContentCqDistributeTypeHint) {
    this.jcrContentCqDistributeTypeHint = jcrContentCqDistributeTypeHint;
  }

  public String getJcrContentCqName() {
    return jcrContentCqName;
  }

  public void setJcrContentCqName(String jcrContentCqName) {
    this.jcrContentCqName = jcrContentCqName;
  }

  /** Sets the agent's {@code jcr:content/cq:name} property. */
  public PostAgentWithHttpInfoRequest withJcrContentCqName(String jcrContentCqName) {
    this.setJcrContentCqName(jcrContentCqName);
    return this;
  }

  public String getJcrContentJcrTitle() {
    return jcrContentJcrTitle;
  }

  public void setJcrContentJcrTitle(String jcrContentJcrTitle) {
    this.jcrContentJcrTitle = jcrContentJcrTitle;
  }

  /** Sets the agent's {@code jcr:content/jcr:title} property. */
  public PostAgentWithHttpInfoRequest withJcrContentJcrTitle(String jcrContentJcrTitle) {
    this.setJcrContentJcrTitle(jcrContentJcrTitle);
    return this;
  }

  public String getJcrContentJcrDescription() {
    return jcrContentJcrDescription;
  }

  public void setJcrContentJcrDescription(String jcrContentJcrDescription) {
    this.jcrContentJcrDescription = jcrContentJcrDescription;
  }

  /** Sets the agent's {@code jcr:content/jcr:description} property. */
  public PostAgentWithHttpInfoRequest withJcrContentJcrDescription(
      String jcrContentJcrDescription) {
    this.setJcrContentJcrDescription(jcrContentJcrDescription);
    return this;
  }

  public String getJcrContentSlingResourceType() {
    return jcrContentSlingResourceType;
  }

  public void setJcrContentSlingResourceType(String jcrContentSlingResourceType) {
    this.jcrContentSlingResourceType = jcrContentSlingResourceType;
  }

  /** Sets the agent's {@code jcr:content/sling:resourceType} property. */
  public PostAgentWithHttpInfoRequest withJcrContentSlingResourceType(
      String jcrContentSlingResourceType) {
    this.setJcrContentSlingResourceType(jcrContentSlingResourceType);
    return this;
  }

  public String getJcrContentTransportUri() {
    return jcrContentTransportUri;
  }

  public void setJcrContentTransportUri(String jcrContentTransportUri) {
    this.jcrContentTransportUri = jcrContentTransportUri;
  }

  /** Sets the agent's {@code jcr:content/transportUri} property. */
  public PostAgentWithHttpInfoRequest withJcrContentTransportUri(String jcrContentTransportUri) {
    this.setJcrContentTransportUri(jcrContentTransportUri);
    return this;
  }

  public String getJcrContentTransportUser() {
    return jcrContentTransportUser;
  }

  public void setJcrContentTransportUser(String jcrContentTransportUser) {
    this.jcrContentTransportUser = jcrContentTransportUser;
  }

  /** Sets the agent's {@code jcr:content/transportUser} property. */
  public PostAgentWithHttpInfoRequest withJcrContentTransportUser(String jcrContentTransportUser) {
    this.setJcrContentTransportUser(jcrContentTransportUser);
    return this;
  }

  public String getJcrContentTransportPassword() {
    return jcrContentTransportPassword;
  }

  public void setJcrContentTransportPassword(String jcrContentTransportPassword) {
    this.jcrContentTransportPassword = jcrContentTransportPassword;
  }

  /** Sets the agent's {@code jcr:content/transportPassword} property. */
  public PostAgentWithHttpInfoRequest withJcrContentTransportPassword(
      String jcrContentTransportPassword) {
    this.setJcrContentTransportPassword(jcrContentTransportPassword);
    return this;
  }

  public String getJcrContentLogLevel() {
    return jcrContentLogLevel;
  }

  public void setJcrContentLogLevel(String jcrContentLogLevel) {
    this.jcrContentLogLevel = jcrContentLogLevel;
  }

  /** Sets the agent's {@code jcr:content/logLevel} property. */
  public PostAgentWithHttpInfoRequest withJcrContentLogLevel(String jcrContentLogLevel) {
    this.setJcrContentLogLevel(jcrContentLogLevel);
    return this;
  }

  public void setJcrContentNoVersioning(Boolean jcrContentNoVersioning) {
    this.jcrContentNoVersioning = jcrContentNoVersioning;
  }

  /** Sets the agent's {@code jcr:content/noVersioning} property. */
  public PostAgentWithHttpInfoRequest withJcrContentNoVersioning(Boolean jcrContentNoVersioning) {
    this.setJcrContentNoVersioning(jcrContentNoVersioning);
    return this;
  }

  /** Returns the agent's HTTP headers as an unmodifiable list, or {@code null} if unset. */
  public List<String> getJcrContentProtocolHttpHeaders() {
    return jcrContentProtocolHttpHeaders == null
        ? null
        : Collections.unmodifiableList(jcrContentProtocolHttpHeaders);
  }

  /** Stores an unmodifiable copy of the given headers; {@code null} unsets them. */
  public void setJcrContentProtocolHttpHeaders(List<String> jcrContentProtocolHttpHeaders) {
    this.jcrContentProtocolHttpHeaders =
        jcrContentProtocolHttpHeaders == null
            ? null
            : new ArrayList<>(jcrContentProtocolHttpHeaders);
  }

  /** Sets the agent's {@code jcr:content/protocolHTTPHeaders} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProtocolHttpHeaders(
      List<String> jcrContentProtocolHttpHeaders) {
    this.setJcrContentProtocolHttpHeaders(jcrContentProtocolHttpHeaders);
    return this;
  }

  public String getJcrContentProtocolHttpHeadersTypeHint() {
    return jcrContentProtocolHttpHeadersTypeHint;
  }

  public void setJcrContentProtocolHttpHeadersTypeHint(
      String jcrContentProtocolHttpHeadersTypeHint) {
    this.jcrContentProtocolHttpHeadersTypeHint = jcrContentProtocolHttpHeadersTypeHint;
  }

  /** Sets the agent's {@code jcr:content/protocolHTTPHeaders@TypeHint} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProtocolHttpHeadersTypeHint(
      String jcrContentProtocolHttpHeadersTypeHint) {
    this.setJcrContentProtocolHttpHeadersTypeHint(jcrContentProtocolHttpHeadersTypeHint);
    return this;
  }

  public String getJcrContentProtocolHttpMethod() {
    return jcrContentProtocolHttpMethod;
  }

  public void setJcrContentProtocolHttpMethod(String jcrContentProtocolHttpMethod) {
    this.jcrContentProtocolHttpMethod = jcrContentProtocolHttpMethod;
  }

  /** Sets the agent's {@code jcr:content/protocolHTTPMethod} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProtocolHttpMethod(
      String jcrContentProtocolHttpMethod) {
    this.setJcrContentProtocolHttpMethod(jcrContentProtocolHttpMethod);
    return this;
  }

  public String getJcrContentRetryDelay() {
    return jcrContentRetryDelay;
  }

  public void setJcrContentRetryDelay(String jcrContentRetryDelay) {
    this.jcrContentRetryDelay = jcrContentRetryDelay;
  }

  /** Sets the agent's {@code jcr:content/retryDelay} property. */
  public PostAgentWithHttpInfoRequest withJcrContentRetryDelay(String jcrContentRetryDelay) {
    this.setJcrContentRetryDelay(jcrContentRetryDelay);
    return this;
  }

  public String getJcrContentSerializationType() {
    return jcrContentSerializationType;
  }

  public void setJcrContentSerializationType(String jcrContentSerializationType) {
    this.jcrContentSerializationType = jcrContentSerializationType;
  }

  /** Sets the agent's {@code jcr:content/serializationType} property. */
  public PostAgentWithHttpInfoRequest withJcrContentSerializationType(
      String jcrContentSerializationType) {
    this.setJcrContentSerializationType(jcrContentSerializationType);
    return this;
  }

  public String getJcrContentJcrMixinTypes() {
    return jcrContentJcrMixinTypes;
  }

  public void setJcrContentJcrMixinTypes(String jcrContentJcrMixinTypes) {
    this.jcrContentJcrMixinTypes = jcrContentJcrMixinTypes;
  }

  /** Sets the agent's {@code jcr:content/jcr:mixinTypes} property. */
  public PostAgentWithHttpInfoRequest withJcrContentJcrMixinTypes(String jcrContentJcrMixinTypes) {
    this.setJcrContentJcrMixinTypes(jcrContentJcrMixinTypes);
    return this;
  }

  public void setJcrContentTriggerReceive(Boolean jcrContentTriggerReceive) {
    this.jcrContentTriggerReceive = jcrContentTriggerReceive;
  }

  /** Sets the agent's {@code jcr:content/triggerReceive} property. */
  public PostAgentWithHttpInfoRequest withJcrContentTriggerReceive(
      Boolean jcrContentTriggerReceive) {
    this.setJcrContentTriggerReceive(jcrContentTriggerReceive);
    return this;
  }

  public void setJcrContentTriggerSpecific(Boolean jcrContentTriggerSpecific) {
    this.jcrContentTriggerSpecific = jcrContentTriggerSpecific;
  }

  /** Sets the agent's {@code jcr:content/triggerSpecific} property. */
  public PostAgentWithHttpInfoRequest withJcrContentTriggerSpecific(
      Boolean jcrContentTriggerSpecific) {
    this.setJcrContentTriggerSpecific(jcrContentTriggerSpecific);
    return this;
  }

  public String getJcrContentCqTemplate() {
    return jcrContentCqTemplate;
  }

  public void setJcrContentCqTemplate(String jcrContentCqTemplate) {
    this.jcrContentCqTemplate = jcrContentCqTemplate;
  }

  /** Sets the agent's {@code jcr:content/cq:template} property. */
  public PostAgentWithHttpInfoRequest withJcrContentCqTemplate(String jcrContentCqTemplate) {
    this.setJcrContentCqTemplate(jcrContentCqTemplate);
    return this;
  }

  public void setJcrContentEnabled(Boolean jcrContentEnabled) {
    this.jcrContentEnabled = jcrContentEnabled;
  }

  /** Sets the agent's {@code jcr:content/enabled} property. */
  public PostAgentWithHttpInfoRequest withJcrContentEnabled(Boolean jcrContentEnabled) {
    this.setJcrContentEnabled(jcrContentEnabled);
    return this;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  /** Sets the Sling POST {@code :operation}, e.g. {@code delete} to remove the agent. */
  public PostAgentWithHttpInfoRequest withOperation(String operation) {
    this.setOperation(operation);
    return this;
  }

  public void setJcrContentTriggerDistribute(Boolean jcrContentTriggerDistribute) {
    this.jcrContentTriggerDistribute = jcrContentTriggerDistribute;
  }

  /** Sets the agent's {@code jcr:content/triggerDistribute} property. */
  public PostAgentWithHttpInfoRequest withJcrContentTriggerDistribute(
      Boolean jcrContentTriggerDistribute) {
    this.setJcrContentTriggerDistribute(jcrContentTriggerDistribute);
    return this;
  }

  public void setJcrContentTriggerModified(Boolean jcrContentTriggerModified) {
    this.jcrContentTriggerModified = jcrContentTriggerModified;
  }

  /** Sets the agent's {@code jcr:content/triggerModified} property. */
  public PostAgentWithHttpInfoRequest withJcrContentTriggerModified(
      Boolean jcrContentTriggerModified) {
    this.setJcrContentTriggerModified(jcrContentTriggerModified);
    return this;
  }

  public void setJcrContentProtocolHttpsRelaxed(Boolean jcrContentProtocolHttpsRelaxed) {
    this.jcrContentProtocolHttpsRelaxed = jcrContentProtocolHttpsRelaxed;
  }

  /** Sets the agent's {@code jcr:content/protocolHTTPSRelaxed} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProtocolHttpsRelaxed(
      Boolean jcrContentProtocolHttpsRelaxed) {
    this.setJcrContentProtocolHttpsRelaxed(jcrContentProtocolHttpsRelaxed);
    return this;
  }

  public String getJcrContentJcrLastModified() {
    return jcrContentJcrLastModified;
  }

  public void setJcrContentJcrLastModified(String jcrContentJcrLastModified) {
    this.jcrContentJcrLastModified = jcrContentJcrLastModified;
  }

  /** Sets the agent's {@code jcr:content/jcr:lastModified} property. */
  public PostAgentWithHttpInfoRequest withJcrContentJcrLastModified(
      String jcrContentJcrLastModified) {
    this.setJcrContentJcrLastModified(jcrContentJcrLastModified);
    return this;
  }

  public String getJcrContentJcrLastModifiedBy() {
    return jcrContentJcrLastModifiedBy;
  }

  public void setJcrContentJcrLastModifiedBy(String jcrContentJcrLastModifiedBy) {
    this.jcrContentJcrLastModifiedBy = jcrContentJcrLastModifiedBy;
  }

  /** Sets the agent's {@code jcr:content/jcr:lastModifiedBy} property. */
  public PostAgentWithHttpInfoRequest withJcrContentJcrLastModifiedBy(
      String jcrContentJcrLastModifiedBy) {
    this.setJcrContentJcrLastModifiedBy(jcrContentJcrLastModifiedBy);
    return this;
  }

  public Boolean getJcrContentNoStatusUpdate() {
    return jcrContentNoStatusUpdate;
  }

  public void setJcrContentNoStatusUpdate(Boolean jcrContentNoStatusUpdate) {
    this.jcrContentNoStatusUpdate = jcrContentNoStatusUpdate;
  }

  /** Sets the agent's {@code jcr:content/noStatusUpdate} property. */
  public PostAgentWithHttpInfoRequest withJcrContentNoStatusUpdate(
      Boolean jcrContentNoStatusUpdate) {
    this.setJcrContentNoStatusUpdate(jcrContentNoStatusUpdate);
    return this;
  }

  public Boolean getJcrContentProtocolHttpConnectionClosed() {
    return jcrContentProtocolHttpConnectionClosed;
  }

  public void setJcrContentProtocolHttpConnectionClosed(
      Boolean jcrContentProtocolHttpConnectionClosed) {
    this.jcrContentProtocolHttpConnectionClosed = jcrContentProtocolHttpConnectionClosed;
  }

  /** Sets the agent's {@code jcr:content/protocolHTTPConnectionClosed} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProtocolHttpConnectionClosed(
      Boolean jcrContentProtocolHttpConnectionClosed) {
    this.setJcrContentProtocolHttpConnectionClosed(jcrContentProtocolHttpConnectionClosed);
    return this;
  }

  public BigDecimal getJcrContentProtocolConnectTimeout() {
    return jcrContentProtocolConnectTimeout;
  }

  public void setJcrContentProtocolConnectTimeout(BigDecimal jcrContentProtocolConnectTimeout) {
    this.jcrContentProtocolConnectTimeout = jcrContentProtocolConnectTimeout;
  }

  /** Sets the agent's {@code jcr:content/protocolConnectTimeout} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProtocolConnectTimeout(
      BigDecimal jcrContentProtocolConnectTimeout) {
    this.setJcrContentProtocolConnectTimeout(jcrContentProtocolConnectTimeout);
    return this;
  }

  public Boolean getJcrContentEnabled() {
    return jcrContentEnabled;
  }

  public Boolean getJcrContentNoVersioning() {
    return jcrContentNoVersioning;
  }

  public Boolean getJcrContentProtocolHttpsRelaxed() {
    return jcrContentProtocolHttpsRelaxed;
  }

  public Boolean getJcrContentTriggerDistribute() {
    return jcrContentTriggerDistribute;
  }

  public Boolean getJcrContentTriggerModified() {
    return jcrContentTriggerModified;
  }

  public Boolean getJcrContentTriggerReceive() {
    return jcrContentTriggerReceive;
  }

  public Boolean getJcrContentTriggerSpecific() {
    return jcrContentTriggerSpecific;
  }

  public String getJcrContentProtocolHttpExpired() {
    return jcrContentProtocolHttpExpired;
  }

  public void setJcrContentProtocolHttpExpired(String jcrContentProtocolHttpExpired) {
    this.jcrContentProtocolHttpExpired = jcrContentProtocolHttpExpired;
  }

  /** Sets the agent's {@code jcr:content/protocolHTTPExpired} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProtocolHttpExpired(
      String jcrContentProtocolHttpExpired) {
    this.setJcrContentProtocolHttpExpired(jcrContentProtocolHttpExpired);
    return this;
  }

  public String getJcrContentProtocolInterface() {
    return jcrContentProtocolInterface;
  }

  public void setJcrContentProtocolInterface(String jcrContentProtocolInterface) {
    this.jcrContentProtocolInterface = jcrContentProtocolInterface;
  }

  /** Sets the agent's {@code jcr:content/protocolInterface} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProtocolInterface(
      String jcrContentProtocolInterface) {
    this.setJcrContentProtocolInterface(jcrContentProtocolInterface);
    return this;
  }

  public BigDecimal getJcrContentProtocolSocketTimeout() {
    return jcrContentProtocolSocketTimeout;
  }

  public void setJcrContentProtocolSocketTimeout(BigDecimal jcrContentProtocolSocketTimeout) {
    this.jcrContentProtocolSocketTimeout = jcrContentProtocolSocketTimeout;
  }

  /** Sets the agent's {@code jcr:content/protocolSocketTimeout} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProtocolSocketTimeout(
      BigDecimal jcrContentProtocolSocketTimeout) {
    this.setJcrContentProtocolSocketTimeout(jcrContentProtocolSocketTimeout);
    return this;
  }

  public String getJcrContentProtocolVersion() {
    return jcrContentProtocolVersion;
  }

  public void setJcrContentProtocolVersion(String jcrContentProtocolVersion) {
    this.jcrContentProtocolVersion = jcrContentProtocolVersion;
  }

  /** Sets the agent's {@code jcr:content/protocolVersion} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProtocolVersion(
      String jcrContentProtocolVersion) {
    this.setJcrContentProtocolVersion(jcrContentProtocolVersion);
    return this;
  }

  public String getJcrContentProxyNtlmDomain() {
    return jcrContentProxyNtlmDomain;
  }

  public void setJcrContentProxyNtlmDomain(String jcrContentProxyNtlmDomain) {
    this.jcrContentProxyNtlmDomain = jcrContentProxyNtlmDomain;
  }

  /** Sets the agent's {@code jcr:content/proxyNTLMDomain} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProxyNtlmDomain(
      String jcrContentProxyNtlmDomain) {
    this.setJcrContentProxyNtlmDomain(jcrContentProxyNtlmDomain);
    return this;
  }

  public String getJcrContentProxyNtlmHost() {
    return jcrContentProxyNtlmHost;
  }

  public void setJcrContentProxyNtlmHost(String jcrContentProxyNtlmHost) {
    this.jcrContentProxyNtlmHost = jcrContentProxyNtlmHost;
  }

  /** Sets the agent's {@code jcr:content/proxyNTLMHost} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProxyNtlmHost(String jcrContentProxyNtlmHost) {
    this.setJcrContentProxyNtlmHost(jcrContentProxyNtlmHost);
    return this;
  }

  public String getJcrContentProxyHost() {
    return jcrContentProxyHost;
  }

  public void setJcrContentProxyHost(String jcrContentProxyHost) {
    this.jcrContentProxyHost = jcrContentProxyHost;
  }

  /** Sets the agent's {@code jcr:content/proxyHost} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProxyHost(String jcrContentProxyHost) {
    this.setJcrContentProxyHost(jcrContentProxyHost);
    return this;
  }

  public BigDecimal getJcrContentProxyPort() {
    return jcrContentProxyPort;
  }

  public void setJcrContentProxyPort(BigDecimal jcrContentProxyPort) {
    this.jcrContentProxyPort = jcrContentProxyPort;
  }

  /** Sets the agent's {@code jcr:content/proxyPort} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProxyPort(BigDecimal jcrContentProxyPort) {
    this.setJcrContentProxyPort(jcrContentProxyPort);
    return this;
  }

  public String getJcrContentProxyUser() {
    return jcrContentProxyUser;
  }

  public void setJcrContentProxyUser(String jcrContentProxyUser) {
    this.jcrContentProxyUser = jcrContentProxyUser;
  }

  /** Sets the agent's {@code jcr:content/proxyUser} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProxyUser(String jcrContentProxyUser) {
    this.setJcrContentProxyUser(jcrContentProxyUser);
    return this;
  }

  public String getJcrContentProxyPassword() {
    return jcrContentProxyPassword;
  }

  public void setJcrContentProxyPassword(String jcrContentProxyPassword) {
    this.jcrContentProxyPassword = jcrContentProxyPassword;
  }

  /** Sets the agent's {@code jcr:content/proxyPassword} property. */
  public PostAgentWithHttpInfoRequest withJcrContentProxyPassword(String jcrContentProxyPassword) {
    this.setJcrContentProxyPassword(jcrContentProxyPassword);
    return this;
  }

  public BigDecimal getJcrContentQueueBatchMaxSize() {
    return jcrContentQueueBatchMaxSize;
  }

  public void setJcrContentQueueBatchMaxSize(BigDecimal jcrContentQueueBatchMaxSize) {
    this.jcrContentQueueBatchMaxSize = jcrContentQueueBatchMaxSize;
  }

  /** Sets the agent's {@code jcr:content/queueBatchMaxSize} property. */
  public PostAgentWithHttpInfoRequest withJcrContentQueueBatchMaxSize(
      BigDecimal jcrContentQueueBatchMaxSize) {
    this.setJcrContentQueueBatchMaxSize(jcrContentQueueBatchMaxSize);
    return this;
  }

  public String getJcrContentQueueBatchMode() {
    return jcrContentQueueBatchMode;
  }

  public void setJcrContentQueueBatchMode(String jcrContentQueueBatchMode) {
    this.jcrContentQueueBatchMode = jcrContentQueueBatchMode;
  }

  /** Sets the agent's {@code jcr:content/queueBatchMode} property. */
  public PostAgentWithHttpInfoRequest withJcrContentQueueBatchMode(
      String jcrContentQueueBatchMode) {
    this.setJcrContentQueueBatchMode(jcrContentQueueBatchMode);
    return this;
  }

  public BigDecimal getJcrContentQueueBatchWaitTime() {
    return jcrContentQueueBatchWaitTime;
  }

  public void setJcrContentQueueBatchWaitTime(BigDecimal jcrContentQueueBatchWaitTime) {
    this.jcrContentQueueBatchWaitTime = jcrContentQueueBatchWaitTime;
  }

  /** Sets the agent's {@code jcr:content/queueBatchWaitTime} property. */
  public PostAgentWithHttpInfoRequest withJcrContentQueueBatchWaitTime(
      BigDecimal jcrContentQueueBatchWaitTime) {
    this.setJcrContentQueueBatchWaitTime(jcrContentQueueBatchWaitTime);
    return this;
  }

  public Boolean getJcrContentReverseReplication() {
    return jcrContentReverseReplication;
  }

  public void setJcrContentReverseReplication(Boolean jcrContentReverseReplication) {
    this.jcrContentReverseReplication = jcrContentReverseReplication;
  }

  /** Sets the agent's {@code jcr:content/reverseReplication} property. */
  public PostAgentWithHttpInfoRequest withJcrContentReverseReplication(
      Boolean jcrContentReverseReplication) {
    this.setJcrContentReverseReplication(jcrContentReverseReplication);
    return this;
  }

  public String getJcrContentSsl() {
    return jcrContentSsl;
  }

  public void setJcrContentSsl(String jcrContentSsl) {
    this.jcrContentSsl = jcrContentSsl;
  }

  /** Sets the agent's {@code jcr:content/ssl} property. */
  public PostAgentWithHttpInfoRequest withJcrContentSsl(String jcrContentSsl) {
    this.setJcrContentSsl(jcrContentSsl);
    return this;
  }

  public String getJcrContentTransportNtlmDomain() {
    return jcrContentTransportNtlmDomain;
  }

  public void setJcrContentTransportNtlmDomain(String jcrContentTransportNtlmDomain) {
    this.jcrContentTransportNtlmDomain = jcrContentTransportNtlmDomain;
  }

  /** Sets the agent's {@code jcr:content/transportNTLMDomain} property. */
  public PostAgentWithHttpInfoRequest withJcrContentTransportNtlmDomain(
      String jcrContentTransportNtlmDomain) {
    this.setJcrContentTransportNtlmDomain(jcrContentTransportNtlmDomain);
    return this;
  }

  public String getJcrContentTransportNtlmHost() {
    return jcrContentTransportNtlmHost;
  }

  public void setJcrContentTransportNtlmHost(String jcrContentTransportNtlmHost) {
    this.jcrContentTransportNtlmHost = jcrContentTransportNtlmHost;
  }

  /** Sets the agent's {@code jcr:content/transportNTLMHost} property. */
  public PostAgentWithHttpInfoRequest withJcrContentTransportNtlmHost(
      String jcrContentTransportNtlmHost) {
    this.setJcrContentTransportNtlmHost(jcrContentTransportNtlmHost);
    return this;
  }

  public Boolean getJcrContentTriggerOnOffTime() {
    return jcrContentTriggerOnOffTime;
  }

  public void setJcrContentTriggerOnOffTime(Boolean jcrContentTriggerOnOffTime) {
    this.jcrContentTriggerOnOffTime = jcrContentTriggerOnOffTime;
  }

  /** Sets the agent's {@code jcr:content/triggerOnOffTime} property. */
  public PostAgentWithHttpInfoRequest withJcrContentTriggerOnOffTime(
      Boolean jcrContentTriggerOnOffTime) {
    this.setJcrContentTriggerOnOffTime(jcrContentTriggerOnOffTime);
    return this;
  }

  public String getJcrContentUserId() {
    return jcrContentUserId;
  }

  public void setJcrContentUserId(String jcrContentUserId) {
    this.jcrContentUserId = jcrContentUserId;
  }

  /** Sets the agent's {@code jcr:content/userId} property. */
  public PostAgentWithHttpInfoRequest withJcrContentUserId(String jcrContentUserId) {
    this.setJcrContentUserId(jcrContentUserId);
    return this;
  }
}
