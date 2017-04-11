package com.shinesolutions.aemorchestrator.aem;

import java.math.BigDecimal;
import java.util.List;

public class PostAgentWithHttpInfoRequest {

    private String runMode;
    private String name;
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
    private Boolean jcrContentProtocolHTTPConnectionClosed;
    private String jcrContentProtocolHTTPExpired;
    private List<String> jcrContentProtocolHTTPHeaders;
    private String jcrContentProtocolHTTPHeadersTypeHint;
    private String jcrContentProtocolHTTPMethod;
    private Boolean jcrContentProtocolHTTPSRelaxed;
    private String jcrContentProtocolInterface;
    private BigDecimal jcrContentProtocolSocketTimeout;
    private String jcrContentProtocolVersion;
    private String jcrContentProxyNTLMDomain;
    private String jcrContentProxyNTLMHost;
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
    private String jcrContentSSL;
    private String jcrContentTransportNTLMDomain;
    private String jcrContentTransportNTLMHost;
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

    public PostAgentWithHttpInfoRequest withJcrPrimaryType(String jcrPrimaryType) {
        this.setJcrPrimaryType(jcrPrimaryType);
        return this;
    }

    public String getJcrContentCqName() {
        return jcrContentCqName;
    }

    public void setJcrContentCqName(String jcrContentCqName) {
        this.jcrContentCqName = jcrContentCqName;
    }

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

    public PostAgentWithHttpInfoRequest withJcrContentJcrDescription(String jcrContentJcrDescription) {
        this.setJcrContentJcrDescription(jcrContentJcrDescription);
        return this;
    }

    public String getJcrContentSlingResourceType() {
        return jcrContentSlingResourceType;
    }

    public void setJcrContentSlingResourceType(String jcrContentSlingResourceType) {
        this.jcrContentSlingResourceType = jcrContentSlingResourceType;
    }

    public PostAgentWithHttpInfoRequest withJcrContentSlingResourceType(String jcrContentSlingResourceType) {
        this.setJcrContentSlingResourceType(jcrContentSlingResourceType);
        return this;
    }

    public String getJcrContentTransportUri() {
        return jcrContentTransportUri;
    }

    public void setJcrContentTransportUri(String jcrContentTransportUri) {
        this.jcrContentTransportUri = jcrContentTransportUri;
    }

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

    public PostAgentWithHttpInfoRequest withJcrContentTransportPassword(String jcrContentTransportPassword) {
        this.setJcrContentTransportPassword(jcrContentTransportPassword);
        return this;
    }

    public String getJcrContentLogLevel() {
        return jcrContentLogLevel;
    }

    public void setJcrContentLogLevel(String jcrContentLogLevel) {
        this.jcrContentLogLevel = jcrContentLogLevel;
    }

    public PostAgentWithHttpInfoRequest withJcrContentLogLevel(String jcrContentLogLevel) {
        this.setJcrContentLogLevel(jcrContentLogLevel);
        return this;
    }

    public void setJcrContentNoVersioning(Boolean jcrContentNoVersioning) {
        this.jcrContentNoVersioning = jcrContentNoVersioning;
    }

    public PostAgentWithHttpInfoRequest withJcrContentNoVersioning(Boolean jcrContentNoVersioning) {
        this.setJcrContentNoVersioning(jcrContentNoVersioning);
        return this;
    }

    public List<String> getJcrContentProtocolHTTPHeaders() {
        return jcrContentProtocolHTTPHeaders;
    }

    public void setJcrContentProtocolHTTPHeaders(List<String> jcrContentProtocolHTTPHeaders) {
        this.jcrContentProtocolHTTPHeaders = jcrContentProtocolHTTPHeaders;
    }

    public PostAgentWithHttpInfoRequest withJcrContentProtocolHTTPHeaders(List<String> jcrContentProtocolHTTPHeaders) {
        this.setJcrContentProtocolHTTPHeaders(jcrContentProtocolHTTPHeaders);
        return this;
    }

    public String getJcrContentProtocolHTTPHeadersTypeHint() {
        return jcrContentProtocolHTTPHeadersTypeHint;
    }

    public void setJcrContentProtocolHTTPHeadersTypeHint(String jcrContentProtocolHTTPHeadersTypeHint) {
        this.jcrContentProtocolHTTPHeadersTypeHint = jcrContentProtocolHTTPHeadersTypeHint;
    }

    public PostAgentWithHttpInfoRequest withJcrContentProtocolHTTPHeadersTypeHint(
        String jcrContentProtocolHTTPHeadersTypeHint) {
        this.setJcrContentProtocolHTTPHeadersTypeHint(jcrContentProtocolHTTPHeadersTypeHint);
        return this;
    }

    public String getJcrContentProtocolHTTPMethod() {
        return jcrContentProtocolHTTPMethod;
    }

    public void setJcrContentProtocolHTTPMethod(String jcrContentProtocolHTTPMethod) {
        this.jcrContentProtocolHTTPMethod = jcrContentProtocolHTTPMethod;
    }

    public PostAgentWithHttpInfoRequest withJcrContentProtocolHTTPMethod(String jcrContentProtocolHTTPMethod) {
        this.setJcrContentProtocolHTTPMethod(jcrContentProtocolHTTPMethod);
        return this;
    }

    public String getJcrContentRetryDelay() {
        return jcrContentRetryDelay;
    }

    public void setJcrContentRetryDelay(String jcrContentRetryDelay) {
        this.jcrContentRetryDelay = jcrContentRetryDelay;
    }

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

    public PostAgentWithHttpInfoRequest withJcrContentSerializationType(String jcrContentSerializationType) {
        this.setJcrContentSerializationType(jcrContentSerializationType);
        return this;
    }

    public String getJcrContentJcrMixinTypes() {
        return jcrContentJcrMixinTypes;
    }

    public void setJcrContentJcrMixinTypes(String jcrContentJcrMixinTypes) {
        this.jcrContentJcrMixinTypes = jcrContentJcrMixinTypes;
    }

    public PostAgentWithHttpInfoRequest withJcrContentJcrMixinTypes(String jcrContentJcrMixinTypes) {
        this.setJcrContentJcrMixinTypes(jcrContentJcrMixinTypes);
        return this;
    }

    public void setJcrContentTriggerReceive(Boolean jcrContentTriggerReceive) {
        this.jcrContentTriggerReceive = jcrContentTriggerReceive;
    }

    public PostAgentWithHttpInfoRequest withJcrContentTriggerReceive(Boolean jcrContentTriggerReceive) {
        this.setJcrContentTriggerReceive(jcrContentTriggerReceive);
        return this;
    }

    public void setJcrContentTriggerSpecific(Boolean jcrContentTriggerSpecific) {
        this.jcrContentTriggerSpecific = jcrContentTriggerSpecific;
    }

    public PostAgentWithHttpInfoRequest withJcrContentTriggerSpecific(Boolean jcrContentTriggerSpecific) {
        this.setJcrContentTriggerSpecific(jcrContentTriggerSpecific);
        return this;
    }

    public String getJcrContentCqTemplate() {
        return jcrContentCqTemplate;
    }

    public void setJcrContentCqTemplate(String jcrContentCqTemplate) {
        this.jcrContentCqTemplate = jcrContentCqTemplate;
    }

    public PostAgentWithHttpInfoRequest withJcrContentCqTemplate(String jcrContentCqTemplate) {
        this.setJcrContentCqTemplate(jcrContentCqTemplate);
        return this;
    }

    public void setJcrContentEnabled(Boolean jcrContentEnabled) {
        this.jcrContentEnabled = jcrContentEnabled;
    }

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

    public PostAgentWithHttpInfoRequest withOperation(String operation) {
        this.setOperation(operation);
        return this;
    }

    public void setJcrContentTriggerDistribute(Boolean jcrContentTriggerDistribute) {
        this.jcrContentTriggerDistribute = jcrContentTriggerDistribute;
    }

    public PostAgentWithHttpInfoRequest withJcrContentTriggerDistribute(Boolean jcrContentTriggerDistribute) {
        this.setJcrContentTriggerDistribute(jcrContentTriggerDistribute);
        return this;
    }

    public void setJcrContentTriggerModified(Boolean jcrContentTriggerModified) {
        this.jcrContentTriggerModified = jcrContentTriggerModified;
    }

    public PostAgentWithHttpInfoRequest withJcrContentTriggerModified(Boolean jcrContentTriggerModified) {
        this.setJcrContentTriggerModified(jcrContentTriggerModified);
        return this;
    }

    public void setJcrContentProtocolHTTPSRelaxed(Boolean jcrContentProtocolHTTPSRelaxed) {
        this.jcrContentProtocolHTTPSRelaxed = jcrContentProtocolHTTPSRelaxed;
    }

    public PostAgentWithHttpInfoRequest withJcrContentProtocolHTTPSRelaxed(Boolean jcrContentProtocolHTTPSRelaxed) {
        this.setJcrContentProtocolHTTPSRelaxed(jcrContentProtocolHTTPSRelaxed);
        return this;
    }

    public String getJcrContentJcrLastModified() {
        return jcrContentJcrLastModified;
    }

    public void setJcrContentJcrLastModified(String jcrContentJcrLastModified) {
        this.jcrContentJcrLastModified = jcrContentJcrLastModified;
    }

    public PostAgentWithHttpInfoRequest withJcrContentJcrLastModified(String jcrContentJcrLastModified) {
        this.setJcrContentJcrLastModified(jcrContentJcrLastModified);
        return this;
    }

    public String getJcrContentJcrLastModifiedBy() {
        return jcrContentJcrLastModifiedBy;
    }

    public void setJcrContentJcrLastModifiedBy(String jcrContentJcrLastModifiedBy) {
        this.jcrContentJcrLastModifiedBy = jcrContentJcrLastModifiedBy;
    }

    public PostAgentWithHttpInfoRequest withJcrContentJcrLastModifiedBy(String jcrContentJcrLastModifiedBy) {
        this.setJcrContentJcrLastModifiedBy(jcrContentJcrLastModifiedBy);
        return this;
    }

    public Boolean getJcrContentNoStatusUpdate() {
        return jcrContentNoStatusUpdate;
    }

    public void setJcrContentNoStatusUpdate(Boolean jcrContentNoStatusUpdate) {
        this.jcrContentNoStatusUpdate = jcrContentNoStatusUpdate;
    }

    public PostAgentWithHttpInfoRequest withJcrContentNoStatusUpdate(Boolean jcrContentNoStatusUpdate) {
        this.setJcrContentNoStatusUpdate(jcrContentNoStatusUpdate);
        return this;
    }

    public Boolean getJcrContentProtocolHTTPConnectionClosed() {
        return jcrContentProtocolHTTPConnectionClosed;
    }

    public void setJcrContentProtocolHTTPConnectionClosed(Boolean jcrContentProtocolHTTPConnectionClosed) {
        this.jcrContentProtocolHTTPConnectionClosed = jcrContentProtocolHTTPConnectionClosed;
    }

    public PostAgentWithHttpInfoRequest withJcrContentProtocolHTTPConnectionClosed(
        Boolean jcrContentProtocolHTTPConnectionClosed) {
        this.setJcrContentProtocolHTTPConnectionClosed(jcrContentProtocolHTTPConnectionClosed);
        return this;
    }

    public BigDecimal getJcrContentProtocolConnectTimeout() {
        return jcrContentProtocolConnectTimeout;
    }

    public void setJcrContentProtocolConnectTimeout(BigDecimal jcrContentProtocolConnectTimeout) {
        this.jcrContentProtocolConnectTimeout = jcrContentProtocolConnectTimeout;
    }

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

    public Boolean getJcrContentProtocolHTTPSRelaxed() {
        return jcrContentProtocolHTTPSRelaxed;
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

    public String getJcrContentProtocolHTTPExpired() {
        return jcrContentProtocolHTTPExpired;
    }

    public void setJcrContentProtocolHTTPExpired(String jcrContentProtocolHTTPExpired) {
        this.jcrContentProtocolHTTPExpired = jcrContentProtocolHTTPExpired;
    }
    
    public PostAgentWithHttpInfoRequest withJcrContentProtocolHTTPExpired(String  jcrContentProtocolHTTPExpired) {
        this.setJcrContentProtocolHTTPExpired(jcrContentProtocolHTTPExpired);
        return this;
    }

    public String getJcrContentProtocolInterface() {
        return jcrContentProtocolInterface;
    }

    public void setJcrContentProtocolInterface(String jcrContentProtocolInterface) {
        this.jcrContentProtocolInterface = jcrContentProtocolInterface;
    }
    
    public PostAgentWithHttpInfoRequest withJcrContentProtocolInterface(String jcrContentProtocolInterface) {
        this.setJcrContentProtocolInterface(jcrContentProtocolInterface);
        return this;
    }

    public BigDecimal getJcrContentProtocolSocketTimeout() {
        return jcrContentProtocolSocketTimeout;
    }

    public void setJcrContentProtocolSocketTimeout(BigDecimal jcrContentProtocolSocketTimeout) {
        this.jcrContentProtocolSocketTimeout = jcrContentProtocolSocketTimeout;
    }
    
    public PostAgentWithHttpInfoRequest withJcrContentProtocolSocketTimeout(BigDecimal jcrContentProtocolSocketTimeout) {
        this.setJcrContentProtocolSocketTimeout(jcrContentProtocolSocketTimeout);
        return this;
    }

    public String getJcrContentProtocolVersion() {
        return jcrContentProtocolVersion;
    }

    public void setJcrContentProtocolVersion(String jcrContentProtocolVersion) {
        this.jcrContentProtocolVersion = jcrContentProtocolVersion;
    }
    
    public PostAgentWithHttpInfoRequest withJcrContentProtocolVersion(String jcrContentProtocolVersion) {
        this.setJcrContentProtocolVersion(jcrContentProtocolVersion);
        return this;
    }

    public String getJcrContentProxyNTLMDomain() {
        return jcrContentProxyNTLMDomain;
    }

    public void setJcrContentProxyNTLMDomain(String jcrContentProxyNTLMDomain) {
        this.jcrContentProxyNTLMDomain = jcrContentProxyNTLMDomain;
    }
    
    public PostAgentWithHttpInfoRequest withJcrContentProxyNTLMDomain(String jcrContentProxyNTLMDomain) {
        this.setJcrContentProxyNTLMDomain(jcrContentProxyNTLMDomain);
        return this;
    }

    public String getJcrContentProxyNTLMHost() {
        return jcrContentProxyNTLMHost;
    }

    public void setJcrContentProxyNTLMHost(String jcrContentProxyNTLMHost) {
        this.jcrContentProxyNTLMHost = jcrContentProxyNTLMHost;
    }
    
    public PostAgentWithHttpInfoRequest withJcrContentProxyNTLMHost(String jcrContentProxyNTLMHost) {
        this.setJcrContentProxyNTLMHost(jcrContentProxyNTLMHost);
        return this;
    }

    public String getJcrContentProxyHost() {
        return jcrContentProxyHost;
    }

    public void setJcrContentProxyHost(String jcrContentProxyHost) {
        this.jcrContentProxyHost = jcrContentProxyHost;
    }
    
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
    
    public PostAgentWithHttpInfoRequest withJcrContentQueueBatchMaxSize(BigDecimal jcrContentQueueBatchMaxSize) {
        this.setJcrContentQueueBatchMaxSize(jcrContentQueueBatchMaxSize);
        return this;
    }

    public String getJcrContentQueueBatchMode() {
        return jcrContentQueueBatchMode;
    }

    public void setJcrContentQueueBatchMode(String jcrContentQueueBatchMode) {
        this.jcrContentQueueBatchMode = jcrContentQueueBatchMode;
    }
    
    public PostAgentWithHttpInfoRequest withJcrContentQueueBatchMode(String jcrContentQueueBatchMode) {
        this.setJcrContentQueueBatchMode(jcrContentQueueBatchMode);
        return this;
    }

    public BigDecimal getJcrContentQueueBatchWaitTime() {
        return jcrContentQueueBatchWaitTime;
    }

    public void setJcrContentQueueBatchWaitTime(BigDecimal jcrContentQueueBatchWaitTime) {
        this.jcrContentQueueBatchWaitTime = jcrContentQueueBatchWaitTime;
    }
    
    public PostAgentWithHttpInfoRequest withJcrContentQueueBatchWaitTime(BigDecimal jcrContentQueueBatchWaitTime) {
        this.setJcrContentQueueBatchWaitTime(jcrContentQueueBatchWaitTime);
        return this;
    }

    public Boolean getJcrContentReverseReplication() {
        return jcrContentReverseReplication;
    }

    public void setJcrContentReverseReplication(Boolean jcrContentReverseReplication) {
        this.jcrContentReverseReplication = jcrContentReverseReplication;
    }
    
    public PostAgentWithHttpInfoRequest withJcrContentReverseReplication(Boolean jcrContentReverseReplication) {
        this.setJcrContentReverseReplication(jcrContentReverseReplication);
        return this;
    }

    public String getJcrContentSSL() {
        return jcrContentSSL;
    }

    public void setJcrContentSSL(String jcrContentSSL) {
        this.jcrContentSSL = jcrContentSSL;
    }
    
    public PostAgentWithHttpInfoRequest withJcrContentSSL(String jcrContentSSL) {
        this.setJcrContentSSL(jcrContentSSL);
        return this;
    }

    public String getJcrContentTransportNTLMDomain() {
        return jcrContentTransportNTLMDomain;
    }

    public void setJcrContentTransportNTLMDomain(String jcrContentTransportNTLMDomain) {
        this.jcrContentTransportNTLMDomain = jcrContentTransportNTLMDomain;
    }
    
    public PostAgentWithHttpInfoRequest withJcrContentTransportNTLMDomain(String jcrContentTransportNTLMDomain) {
        this.setJcrContentTransportNTLMDomain(jcrContentTransportNTLMDomain);
        return this;
    }

    public String getJcrContentTransportNTLMHost() {
        return jcrContentTransportNTLMHost;
    }

    public void setJcrContentTransportNTLMHost(String jcrContentTransportNTLMHost) {
        this.jcrContentTransportNTLMHost = jcrContentTransportNTLMHost;
    }
    
    public PostAgentWithHttpInfoRequest withJcrContentTransportNTLMHost(String jcrContentTransportNTLMHost) {
        this.setJcrContentTransportNTLMHost(jcrContentTransportNTLMHost);
        return this;
    }

    public Boolean getJcrContentTriggerOnOffTime() {
        return jcrContentTriggerOnOffTime;
    }

    public void setJcrContentTriggerOnOffTime(Boolean jcrContentTriggerOnOffTime) {
        this.jcrContentTriggerOnOffTime = jcrContentTriggerOnOffTime;
    }
    
    public PostAgentWithHttpInfoRequest withJcrContentTriggerOnOffTime(Boolean jcrContentTriggerOnOffTime) {
        this.setJcrContentTriggerOnOffTime(jcrContentTriggerOnOffTime);
        return this;
    }

    public String getJcrContentUserId() {
        return jcrContentUserId;
    }

    public void setJcrContentUserId(String jcrContentUserId) {
        this.jcrContentUserId = jcrContentUserId;
    }
    
    public PostAgentWithHttpInfoRequest withJcrContentUserId(String jcrContentUserId) {
        this.setJcrContentUserId(jcrContentUserId);
        return this;
    }

}
