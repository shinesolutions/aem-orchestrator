package com.shinesolutions.aemorchestrator.aem;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

@Component
public class AgentRequestFactory {
    
    private static final String SLING_RESOURCE_TYPE = "/libs/cq/replication/components/agent";
    private static final String CQ_TEMPLATE = "/libs/cq/replication/templates/agent";
    
    public PostAgentWithHttpInfoRequest getCreateFlushAgentRequest(AgentRunMode runMode, String agentName, 
        String agentDescription, String aemDispatcherBaseUrl) {

        return new PostAgentWithHttpInfoRequest()
            .withRunMode(runMode.getValue())
            .withName(agentName)
            .withJcrPrimaryType("cq:Page")
            .withJcrContentCqName(null)
            .withJcrContentJcrTitle(agentName)
            .withJcrContentJcrDescription(agentDescription)
            .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE)
            .withJcrContentTransportUri(aemDispatcherBaseUrl + "/dispatcher/invalidate.cache")
            .withJcrContentTransportUser("")
            .withJcrContentTransportPassword("")
            .withJcrContentLogLevel("error")
            .withJcrContentNoVersioning(true)
            .withJcrContentProtocolHTTPHeaders(Arrays.asList("CQ-Action:{action}"))
            .withJcrContentProtocolHTTPHeadersTypeHint("String[]")
            .withJcrContentProtocolHTTPMethod("GET")
            .withJcrContentRetryDelay("" + TimeUnit.MINUTES.toMillis(1))
            .withJcrContentSerializationType("flush")
            .withJcrContentJcrMixinTypes("cq:ReplicationStatus")
            .withJcrContentTriggerReceive(true)
            .withJcrContentTriggerSpecific(true)
            .withJcrContentCqTemplate(CQ_TEMPLATE)
            .withJcrContentEnabled(true);

    }
    
    public PostAgentWithHttpInfoRequest getCreateReplicationAgentRequest(AgentRunMode runMode, String agentName, 
        String agentDescription, String aemBaseUrl, String user, String password) {

        return new PostAgentWithHttpInfoRequest()
            .withRunMode(runMode.getValue())
            .withName(agentName)
            .withJcrPrimaryType("cq:Page")
            .withJcrContentCqName(null)
            .withJcrContentJcrTitle(agentName)
            .withJcrContentJcrDescription(agentDescription)
            .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE)
            .withJcrContentTransportUri(aemBaseUrl + "/bin/receive?sling:authRequestLogin=1")
            .withJcrContentTransportUser(user)
            .withJcrContentTransportPassword(password)
            .withJcrContentLogLevel("error")
            .withJcrContentNoVersioning(false)
            .withJcrContentProtocolHTTPHeaders(Collections.emptyList())
            .withJcrContentProtocolHTTPHeadersTypeHint(null)
            .withJcrContentProtocolHTTPMethod(null)
            .withJcrContentRetryDelay("" + TimeUnit.MINUTES.toMillis(1))
            .withJcrContentSerializationType("durbo")
            .withJcrContentJcrMixinTypes(null)
            .withJcrContentTriggerReceive(false)
            .withJcrContentTriggerSpecific(false)
            .withJcrContentCqTemplate(CQ_TEMPLATE)
            .withJcrContentEnabled(true);

    }
    
    public PostAgentWithHttpInfoRequest getPauseReplicationAgentRequest(
        AgentRunMode runMode, String agentName) {

        return new PostAgentWithHttpInfoRequest()
            .withRunMode(runMode.getValue())
            .withName(agentName)
            .withJcrPrimaryType("cq:Page")
            .withJcrContentCqName(null)
            .withJcrContentJcrTitle(agentName)
            .withJcrContentJcrDescription(null)
            .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE)
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
            .withJcrContentCqTemplate(CQ_TEMPLATE)
            .withJcrContentEnabled(false);

    }
    
    public PostAgentWithHttpInfoRequest getResumeReplicationAgentRequest(
        AgentRunMode runMode, String agentName, String user, String password) {

        return new PostAgentWithHttpInfoRequest()
            .withRunMode(runMode.getValue())
            .withName(agentName)
            .withJcrPrimaryType("cq:Page")
            .withJcrContentCqName(null)
            .withJcrContentJcrTitle(agentName)
            .withJcrContentJcrDescription(null)
            .withJcrContentSlingResourceType(SLING_RESOURCE_TYPE)
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
            .withJcrContentCqTemplate(CQ_TEMPLATE)
            .withJcrContentEnabled(true);

    }
    
    

}
