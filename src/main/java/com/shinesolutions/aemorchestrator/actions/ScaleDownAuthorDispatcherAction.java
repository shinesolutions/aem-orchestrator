package com.shinesolutions.aemorchestrator.actions;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.aem.AgentRunMode;
import com.shinesolutions.aemorchestrator.aem.FlushAgentManager;
import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.swaggeraem4j.ApiException;

@Component
public class ScaleDownAuthorDispatcherAction implements Action {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Resource
    private FlushAgentManager flushAgentManager;
    
    @Resource
    private AemInstanceHelperService aemHelperService;
    
    public boolean execute(String instanceId) {
        logger.info("ScaleDownAuthorDispatcherAction executing");
        
        String aemBasePath = aemHelperService.getAemUrlForAuthorElb();
        
        try {
            logger.debug("Attempting to delete flush agent at base AEM path: " + aemBasePath);
            flushAgentManager.deleteFlushAgent(instanceId, aemBasePath, AgentRunMode.AUTHOR);
            logger.info("Flush Agent removed successfully");
        } catch (ApiException e) {
            logger.warn("Failed to delete flush agent for dispatcher id: " + instanceId + 
                ". It may already have been deleted", e);
        }
        
        return true;
    }

}
