package com.shinesolutions.aemorchestrator.aem;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.swaggeraem4j.ApiClient;
import com.shinesolutions.swaggeraem4j.api.SlingApi;

@Component
public class FlushAgentManager {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Resource
    private ApiClient apiClient;
    
    private SlingApi slingApi;
    
    public FlushAgentManager() {
        this.slingApi = new SlingApi(apiClient);
    }
    
    public boolean deleteFlushAgent(String dispatcherId) {
        logger.info("Deleting flush agent for dispatcher id: " + dispatcherId);
        return false;
    }
    
    
}
