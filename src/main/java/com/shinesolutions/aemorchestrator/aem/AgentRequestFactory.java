package com.shinesolutions.aemorchestrator.aem;

import static com.shinesolutions.aemorchestrator.model.AemSSL.RELAXED;
import static com.shinesolutions.aemorchestrator.model.AemSSL.DEFAULT;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AgentRequestFactory {
    
    @Value("${aem.reverseReplication.transportUri.postfix}")
    private String reverseReplicationTransportUriPostfix;
    
    @Value("${aem.relaxed.ssl.enable}")
    private boolean enableRelaxedSSL;

    @Value("${aem.flush.logLevel}")
    private String flushLogLevel;

    @Value("${aem.replication.logLevel}")
    private String replicationLogLevel;

    @Value("${aem.reverseReplication.logLevel}")
    private String reverseReplicationLogLevel;
    
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
            .withJcrContentJcrTitle(agentName)
            .withJcrContentJcrDescription(agentDescription)
            .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE_REPLICATION_AGENT)
            .withJcrContentTransportUri(aemDispatcherBaseUrl + "/dispatcher/invalidate.cache")
            .withJcrContentTransportUser("")
            .withJcrContentTransportPassword("")
            .withJcrContentLogLevel(getLogType(flushLogLevel))
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
            .withJcrContentSSL(enableRelaxedSSL ? RELAXED.getValue() : DEFAULT.getValue())
            .withJcrContentEnabled(true);
            

    }
    
    public PostAgentWithHttpInfoRequest getCreateReplicationAgentRequest(AgentRunMode runMode, String agentName, 
        String agentDescription, String aemBaseUrl, String user, String password) {

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
            .withJcrContentSSL(enableRelaxedSSL ? RELAXED.getValue() : DEFAULT.getValue())
            .withJcrContentEnabled(true);
            
    }
    
    public PostAgentWithHttpInfoRequest getCreateReverseReplicationAgentRequest(AgentRunMode runMode, String agentName, 
        String agentDescription, String aemBaseUrl, String user, String password) {

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
            .withJcrContentProtocolHTTPHeaders(Collections.emptyList())
            .withJcrContentProtocolHTTPHeadersTypeHint(null)
            .withJcrContentProtocolHTTPMethod("GET")
            .withJcrContentRetryDelay("" + TimeUnit.MINUTES.toMillis(1))
            .withJcrContentSerializationType("durbo")
            .withJcrContentCqTemplate(CQ_TEMPLATE_REVERSE_REPLICATION_AGENT)
            .withJcrContentReverseReplication(true)
            .withJcrContentSSL(enableRelaxedSSL ? RELAXED.getValue() : DEFAULT.getValue())
            .withJcrContentEnabled(true);
    }
    
    public PostAgentWithHttpInfoRequest getPauseReplicationAgentRequest(
        AgentRunMode runMode, String agentName) {

        return new PostAgentWithHttpInfoRequest()
            .withRunMode(runMode.getValue())
            .withName(agentName)
            .withJcrPrimaryType(JCR_PRIMARY_TYPE)
            .withJcrContentJcrTitle(agentName)
            .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE_REPLICATION_AGENT)
            .withJcrContentTransportUser("orchestrator-pause") // Is meant be an invalid user
            .withJcrContentCqTemplate(CQ_TEMPLATE_REPLICATION_AGENT)
            .withJcrContentEnabled(false);

    }
    
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
    
    public PostAgentWithHttpInfoRequest getDeleteAgentRequest(AgentRunMode runMode, String agentName) {
        return new PostAgentWithHttpInfoRequest()
            .withRunMode(runMode.getValue())
            .withName(agentName)
            .withOperation(OPERATION_DELETE);
    }

    private String getLogType(String agentLogLevel) {
        return (agentLogLevel != null && !agentLogLevel.isEmpty()) ? agentLogLevel : DEFAULT_LOG_LEVEL;
    }
}
