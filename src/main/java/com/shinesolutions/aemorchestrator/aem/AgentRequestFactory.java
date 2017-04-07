package com.shinesolutions.aemorchestrator.aem;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AgentRequestFactory {
    
    @Value("${aem.reverseReplication.transportUri.postfix}")
    private String reverseReplicationTransportUriPostfix;
    
    private static final String JCR_PRIMARY_TYPE = "cq:Page";
    private static final String SLING_RESOURCE_TYPE_REPLICATION_AGENT = "cq/replication/components/agent";
    private static final String CQ_TEMPLATE_REPLICATION_AGENT = "/libs/cq/replication/templates/agent";
    private static final String SLING_RESOURCE_TYPE_REVERSE_REPLICATION_AGENT = "cq/replication/components/revagent";
    private static final String CQ_TEMPLATE_REVERSE_REPLICATION_AGENT = "/libs/cq/replication/templates/revagent";
    private static final String TRANSPORT_URI_POSTFIX = "/bin/receive?sling:authRequestLogin=1";
    private static final String DEFAULT_LOG_LEVEL = "info";
    private static final String OPERATION_DELETE = "delete";
    
    public PostAgentWithHttpInfoRequest getCreateFlushAgentRequest(AgentRunMode runMode, String agentName, 
        String agentDescription, String aemDispatcherBaseUrl) {

        return new PostAgentWithHttpInfoRequest()
            .withRunMode(runMode.getValue())
            .withName(agentName)
            .withJcrPrimaryType(JCR_PRIMARY_TYPE)
            .withJcrContentCqName(null)
            .withJcrContentJcrTitle(agentName)
            .withJcrContentJcrDescription(agentDescription)
            .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE_REPLICATION_AGENT)
            .withJcrContentTransportUri(aemDispatcherBaseUrl + "/dispatcher/invalidate.cache")
            .withJcrContentTransportUser("")
            .withJcrContentTransportPassword("")
            .withJcrContentLogLevel(DEFAULT_LOG_LEVEL)
            .withJcrContentNoVersioning(true)
            .withJcrContentProtocolHTTPHeaders(Arrays.asList("CQ-Action:{action}"))
            .withJcrContentProtocolHTTPHeadersTypeHint("String[]")
            .withJcrContentProtocolHTTPMethod("GET")
            .withJcrContentRetryDelay("" + TimeUnit.MINUTES.toMillis(1))
            .withJcrContentSerializationType("flush")
            .withJcrContentJcrMixinTypes("cq:ReplicationStatus")
            .withJcrContentTriggerReceive(true)
            .withJcrContentTriggerSpecific(true)
            .withJcrContentCqTemplate(CQ_TEMPLATE_REPLICATION_AGENT)
            .withJcrContentEnabled(true);

    }
    
    public PostAgentWithHttpInfoRequest getCreateReplicationAgentRequest(AgentRunMode runMode, String agentName, 
        String agentDescription, String aemBaseUrl, String user, String password) {

        return new PostAgentWithHttpInfoRequest()
            .withRunMode(runMode.getValue())
            .withName(agentName)
            .withJcrPrimaryType(JCR_PRIMARY_TYPE)
            .withJcrContentCqName(null)
            .withJcrContentJcrTitle(agentName)
            .withJcrContentJcrDescription(agentDescription)
            .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE_REPLICATION_AGENT)
            .withJcrContentTransportUri(aemBaseUrl + TRANSPORT_URI_POSTFIX)
            .withJcrContentTransportUser(user)
            .withJcrContentTransportPassword(password)
            .withJcrContentLogLevel(DEFAULT_LOG_LEVEL)
            .withJcrContentNoVersioning(false)
            .withJcrContentProtocolHTTPHeaders(Collections.emptyList())
            .withJcrContentProtocolHTTPHeadersTypeHint(null)
            .withJcrContentProtocolHTTPMethod(null)
            .withJcrContentRetryDelay("" + TimeUnit.MINUTES.toMillis(1))
            .withJcrContentSerializationType("durbo")
            .withJcrContentJcrMixinTypes(null)
            .withJcrContentTriggerReceive(false)
            .withJcrContentTriggerSpecific(false)
            .withJcrContentCqTemplate(CQ_TEMPLATE_REPLICATION_AGENT)
            .withJcrContentEnabled(true);
            
    }
    
    public PostAgentWithHttpInfoRequest getCreateReverseReplicationAgentRequest(AgentRunMode runMode, String agentName, 
        String agentDescription, String aemBaseUrl, String user, String password) {

        return new PostAgentWithHttpInfoRequest()
            .withRunMode(runMode.getValue())
            .withName(agentName)
            .withJcrPrimaryType(JCR_PRIMARY_TYPE)
            .withJcrContentCqName(null)
            .withJcrContentJcrTitle(agentName)
            .withJcrContentJcrDescription(agentDescription)
            .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE_REVERSE_REPLICATION_AGENT)
            .withJcrContentTransportUri(aemBaseUrl + reverseReplicationTransportUriPostfix)
            .withJcrContentTransportUser(user)
            .withJcrContentTransportPassword(password)
            .withUserId(user)
            .withJcrContentLogLevel(DEFAULT_LOG_LEVEL)
            .withJcrContentNoVersioning(false)
            .withJcrContentProtocolHTTPHeaders(Collections.emptyList())
            .withJcrContentProtocolHTTPHeadersTypeHint(null)
            .withJcrContentProtocolHTTPMethod("GET")
            .withJcrContentRetryDelay("" + TimeUnit.MINUTES.toMillis(1))
            .withJcrContentSerializationType("durbo")
            .withJcrContentJcrMixinTypes(null)
            .withJcrContentTriggerReceive(false)
            .withJcrContentTriggerSpecific(false)
            .withJcrContentCqTemplate(CQ_TEMPLATE_REVERSE_REPLICATION_AGENT)
            .withJcrContentEnabled(true)
            .withJcrReverseReplication(true);
    }
    
    public PostAgentWithHttpInfoRequest getPauseReplicationAgentRequest(
        AgentRunMode runMode, String agentName) {

        return new PostAgentWithHttpInfoRequest()
            .withRunMode(runMode.getValue())
            .withName(agentName)
            .withJcrPrimaryType(JCR_PRIMARY_TYPE)
            .withJcrContentCqName(null)
            .withJcrContentJcrTitle(agentName)
            .withJcrContentJcrDescription(null)
            .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE_REPLICATION_AGENT)
            .withJcrContentTransportUri(null)
            .withJcrContentTransportUser("orchestrator-pause") // Is meant be an invalid user
            .withJcrContentTransportPassword(null)
            .withJcrContentLogLevel(null)
            .withJcrContentNoVersioning(false)
            .withJcrContentProtocolHTTPHeaders(Collections.emptyList())
            .withJcrContentProtocolHTTPHeadersTypeHint(null)
            .withJcrContentProtocolHTTPMethod(null)
            .withJcrContentRetryDelay(null)
            .withJcrContentSerializationType(null)
            .withJcrContentJcrMixinTypes(null)
            .withJcrContentTriggerReceive(false)
            .withJcrContentTriggerSpecific(false)
            .withJcrContentCqTemplate(CQ_TEMPLATE_REPLICATION_AGENT)
            .withJcrContentEnabled(false);

    }
    
    public PostAgentWithHttpInfoRequest getResumeReplicationAgentRequest(
        AgentRunMode runMode, String agentName, String user, String password) {

        return new PostAgentWithHttpInfoRequest()
            .withRunMode(runMode.getValue())
            .withName(agentName)
            .withJcrPrimaryType(JCR_PRIMARY_TYPE)
            .withJcrContentCqName(null)
            .withJcrContentJcrTitle(agentName)
            .withJcrContentJcrDescription(null)
            .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE_REPLICATION_AGENT)
            .withJcrContentTransportUri(null)
            .withJcrContentTransportUser(user)
            .withJcrContentTransportPassword(password)
            .withJcrContentLogLevel(null)
            .withJcrContentNoVersioning(false)
            .withJcrContentProtocolHTTPHeaders(Collections.emptyList())
            .withJcrContentProtocolHTTPHeadersTypeHint(null)
            .withJcrContentProtocolHTTPMethod(null)
            .withJcrContentRetryDelay(null)
            .withJcrContentSerializationType(null)
            .withJcrContentJcrMixinTypes(null)
            .withJcrContentTriggerReceive(false)
            .withJcrContentTriggerSpecific(false)
            .withJcrContentCqTemplate(CQ_TEMPLATE_REPLICATION_AGENT)
            .withJcrContentEnabled(true);

    }
    
    public PostAgentWithHttpInfoRequest getDeleteAgentRequest(AgentRunMode runMode, String agentName) {
        return new PostAgentWithHttpInfoRequest()
            .withRunMode(runMode.getValue())
            .withName(agentName)
            .withOperation(OPERATION_DELETE);
    }
}
