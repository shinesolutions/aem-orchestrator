package com.shinesolutions.aemorchestrator.aem;

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
    
    public boolean deleteFlushAgent(String instanceId, AgentRunMode runMode) {
        logger.info("Deleting flush agent for dispatcher id: " + instanceId + 
            ", and run mode: " + runMode.name());
        
        ApiResponse<Void> response;
        try {
            SlingApi slingApi = aemApiFactory.getSlingApi(runMode, AgentAction.DELETE, instanceId);
            response = slingApi.deleteAgentWithHttpInfo(runMode.name().toLowerCase(), instanceId);
            logger.debug("ApiResponse status code: " + response.getStatusCode());

        } catch (ApiException e) {
            logger.error("Failed to delete dispatcher", e);
        }
        
        //TODO this need to set this based on ApiResponse
        return true;
    }
    
    
}
