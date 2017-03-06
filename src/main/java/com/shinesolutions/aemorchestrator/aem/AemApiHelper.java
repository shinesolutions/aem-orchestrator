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
    
    public ApiResponse<Void> postAgentWithHttpInfo(SlingApi slingApi, PostAgentWithHttpInfoRequest request) throws ApiException {
        return slingApi.postAgentWithHttpInfo(request.getRunMode(), request.getName(),
            request.getJcrPrimaryType(), request.getJcrContentCqName(), request.getJcrContentJcrTitle(),
            request.getJcrContentJcrDescription(), request.getJcrContentSlingResourceType(),
            request.getJcrContentTransportUri(), request.getJcrContentTransportUser(),
            request.getJcrContentTransportPassword(), request.getJcrContentLogLevel(),
            request.isJcrContentNoVersioning(), request.getJcrContentProtocolHTTPHeaders(),
            request.getJcrContentProtocolHTTPHeadersTypeHint(), request.getJcrContentProtocolHTTPMethod(),
            request.getJcrContentRetryDelay(), request.getJcrContentSerializationType(),
            request.getJcrContentJcrMixinTypes(), request.isJcrContentTriggerReceive(),
            request.isJcrContentTriggerSpecific(), request.getJcrContentCqTemplate(), request.isJcrContentEnabled(),
            request.isJcrReverseReplication(), request.getOperation());
    }
    

}
