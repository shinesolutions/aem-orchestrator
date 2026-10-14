package com.shinesolutions.aemorchestrator.aem;

import com.shinesolutions.swaggeraem4j.ApiException;
import com.shinesolutions.swaggeraem4j.ApiResponse;
import com.shinesolutions.swaggeraem4j.api.SlingApi;
import org.springframework.stereotype.Component;

/** Wrapper for making calls to the AEM Client API easier. */
@Component
public class AemApiHelper {

  /**
   * Creates, updates or deletes an AEM replication/flush agent by unpacking {@code request} into
   * the positional arguments of {@link SlingApi#postAgentWithHttpInfo}.
   *
   * @param slingApi client for the AEM instance hosting the agent
   * @param request agent run mode, name and properties to post; {@code null} values are omitted
   * @return the API response, used by callers for its status code
   * @throws ApiException if the AEM request fails or returns an error status
   */
  public ApiResponse<Void> postAgentWithHttpInfo(
      SlingApi slingApi, PostAgentWithHttpInfoRequest request) throws ApiException {
    return slingApi.postAgentWithHttpInfo(
        request.getRunMode(),
        request.getName(),
        request.getJcrContentCqDistribute(),
        request.getJcrContentCqDistributeTypeHint(),
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
        request.getJcrContentProtocolHttpConnectionClosed(),
        request.getJcrContentProtocolHttpExpired(),
        request.getJcrContentProtocolHttpHeaders(),
        request.getJcrContentProtocolHttpHeadersTypeHint(),
        request.getJcrContentProtocolHttpMethod(),
        request.getJcrContentProtocolHttpsRelaxed(),
        request.getJcrContentProtocolInterface(),
        request.getJcrContentProtocolSocketTimeout(),
        request.getJcrContentProtocolVersion(),
        request.getJcrContentProxyNtlmDomain(),
        request.getJcrContentProxyNtlmHost(),
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
        request.getJcrContentSsl(),
        request.getJcrContentTransportNtlmDomain(),
        request.getJcrContentTransportNtlmHost(),
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
