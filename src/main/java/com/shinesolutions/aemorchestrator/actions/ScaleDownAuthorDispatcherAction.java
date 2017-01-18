package com.shinesolutions.aemorchestrator.actions;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.aem.AgentRunMode;
import com.shinesolutions.aemorchestrator.aem.FlushAgentManager;
import com.shinesolutions.aemorchestrator.service.AemLookupService;
import com.shinesolutions.swaggeraem4j.ApiException;

@Component
public class ScaleDownAuthorDispatcherAction implements ScaleAction {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Resource
    private FlushAgentManager flushAgentManager;
    
    @Resource
    private AemLookupService aemLookupService;
    
    public boolean execute(String instanceId) {
        logger.info("ScaleDownAuthorDispatcherAction executing");
        
        String aemBasePath = aemLookupService.getAemUrlForAuthorElb();
        
        boolean success = false;
        
        try {
            flushAgentManager.deleteFlushAgent(instanceId, aemBasePath, AgentRunMode.AUTHOR);
            success = true;
        } catch (ApiException e) {
            logger.error("Failed to delete flush agent for dispatcher id: " + instanceId + ", and run mode: "
                + AgentRunMode.AUTHOR.name(), e);
        }
        
        return success;
    }

}
