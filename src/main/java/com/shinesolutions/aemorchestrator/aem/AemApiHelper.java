package com.shinesolutions.aemorchestrator.aem;

import org.springframework.stereotype.Component;

import com.shinesolutions.swaggeraem4j.ApiException;
import com.shinesolutions.swaggeraem4j.ApiResponse;
import com.shinesolutions.swaggeraem4j.api.SlingApi;

/**
 * Wrapper for making cals to the AEM Client API easier
 *
 */
@Component
public class AemApiHelper {

    public ApiResponse<Void> postAgentWithHttpInfo(SlingApi slingApi, PostAgentWithHttpInfoRequest request)
        throws ApiException {
        return slingApi.postAgentWithHttpInfo(
            request.getRunMode(), 
            request.getName(), 
            request.getJcrContentCqName(), 
            request.getJcrContentCqTemplate(),
            request.getJcrContentEnabled(),
            request.getJcrContentJcrDescription(),
            request.getJcrContentJcrLastModified(),
            request.getJcrContentJcrLastModifiedBy(),
            request.getJcrContentJcrMixinTypes(),
            request.getJcrContentJcrTitle(),
            request.getJcrContentLogLevel(),
            request.getJcrContentNoStatusUpdate(),
            request.getJcrContentNoVersioning(),
            request.getJcrContentProtocolConnectTimeout(),
            request.getJcrContentProtocolHTTPConnectionClosed(),
            request.getJcrContentProtocolHTTPExpired(),
            request.getJcrContentProtocolHTTPHeaders(),
            request.getJcrContentProtocolHTTPHeadersTypeHint(),
            request.getJcrContentProtocolHTTPMethod(),
            request.getJcrContentProtocolHTTPSRelaxed(),
            request.getJcrContentProtocolInterface(),
            request.getJcrContentProtocolSocketTimeout(),
            request.getJcrContentProtocolVersion(),
            request.getJcrContentProxyNTLMDomain(),
            request.getJcrContentProxyNTLMHost(),
            request.getJcrContentProxyHost(),
            request.getJcrContentProxyPassword(),
            request.getJcrContentProxyPort(),
            request.getJcrContentProxyUser(),
            request.getJcrContentQueueBatchMaxSize(),
            request.getJcrContentQueueBatchMode(),
            request.getJcrContentQueueBatchWaitTime(),
            request.getJcrContentRetryDelay(),
            request.getJcrContentReverseReplication(),
            request.getJcrContentSerializationType(),
            request.getJcrContentSlingResourceType(),
            request.getJcrContentSSL(),
            request.getJcrContentTransportNTLMDomain(),
            request.getJcrContentTransportNTLMHost(),
            request.getJcrContentTransportPassword(),
            request.getJcrContentTransportUri(),
            request.getJcrContentTransportUser(),
            request.getJcrContentTriggerDistribute(),
            request.getJcrContentTriggerModified(),
            request.getJcrContentTriggerOnOffTime(),
            request.getJcrContentTriggerReceive(),
            request.getJcrContentTriggerSpecific(),
            request.getJcrContentUserId(),
            request.getJcrPrimaryType(),
            request.getOperation());
    }

}
