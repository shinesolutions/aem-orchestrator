package com.shinesolutions.aemorchestrator.aem;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.swaggeraem4j.ApiException;
import com.shinesolutions.swaggeraem4j.ApiResponse;
import com.shinesolutions.swaggeraem4j.api.SlingApi;

@Component
public class FlushAgentManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private AemApiFactory aemApiFactory;

    public void deleteFlushAgent(String dispatcherInstanceId, String aemBaseUrl, AgentRunMode runMode)
        throws ApiException {
        logger.info(
            "Deleting flush agent for dispatcher id: " + dispatcherInstanceId + ", and run mode: " + runMode.name());

        ApiResponse<Void> response;

        SlingApi slingApi = aemApiFactory.getSlingApi(aemBaseUrl, AgentAction.DELETE);
        response = slingApi.deleteAgentWithHttpInfo(runMode.name().toLowerCase(),
            getFlushAgentName(dispatcherInstanceId));
        logger.debug("ApiResponse status code: " + response.getStatusCode());

    }

    public void createFlushAgent(String dispatcherInstanceId, String aemBaseUrl, String aemDispatcherBaseUrl,
        AgentRunMode runMode) throws ApiException {
        logger.info(
            "Creating flush agent for dispatcher id: " + dispatcherInstanceId + ", and run mode: " + runMode.name());

        ApiResponse<Void> response;

        String name = getFlushAgentName(dispatcherInstanceId);
        String jcrPrimaryType = "cq:Page";
        String jcrContentCqName = "";
        String jcrContentJcrTitle = name;
        String jcrContentJcrDescription = "Flush Agent for Dispatcher";
        String jcrContentSlingResourceType = "/libs/cq/replication/components/agent";
        String jcrContentTransportUri = aemDispatcherBaseUrl + "/dispatcher/invalidate.cache";
        String jcrContentTransportUser = "";
        String jcrContentTransportPassword = "";
        String jcrContentLogLevel = "error";
        boolean jcrContentNoVersioning = true;
        List<String> jcrContentProtocolHTTPHeaders = Arrays.asList("CQ-Action:{action}");
        String jcrContentProtocolHTTPHeadersTypeHint = "String[]";
        String jcrContentProtocolHTTPMethod = "GET";
        String jcrContentRetryDelay = "" + TimeUnit.MINUTES.toMillis(1);
        String jcrContentSerializationType = "flush";
        String jcrContentJcrMixinTypes = "cq:ReplicationStatus";
        boolean jcrContentTriggerReceive = true;
        boolean jcrContentTriggerSpecific = true;
        String jcrContentCqTemplate = "/libs/cq/replication/templates/agent";
        boolean jcrContentEnabled = true;

        SlingApi slingApi = aemApiFactory.getSlingApi(aemBaseUrl, AgentAction.CREATE);

        response = slingApi.postAgentWithHttpInfo(runMode.name().toLowerCase(), name, jcrPrimaryType, jcrContentCqName,
            jcrContentJcrTitle, jcrContentJcrDescription, jcrContentSlingResourceType, jcrContentTransportUri,
            jcrContentTransportUser, jcrContentTransportPassword, jcrContentLogLevel, jcrContentNoVersioning,
            jcrContentProtocolHTTPHeaders, jcrContentProtocolHTTPHeadersTypeHint, jcrContentProtocolHTTPMethod,
            jcrContentRetryDelay, jcrContentSerializationType, jcrContentJcrMixinTypes, jcrContentTriggerReceive,
            jcrContentTriggerSpecific, jcrContentCqTemplate, jcrContentEnabled);

        logger.debug("ApiResponse status code: " + response.getStatusCode());

    }

    private String getFlushAgentName(String instanceId) {
        return "flushAgent-" + instanceId;
    }

}
