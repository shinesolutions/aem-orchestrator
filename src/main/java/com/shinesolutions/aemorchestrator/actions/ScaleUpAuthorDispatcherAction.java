package com.shinesolutions.aemorchestrator.actions;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.aem.AgentRunMode;
import com.shinesolutions.aemorchestrator.aem.FlushAgentManager;
import com.shinesolutions.aemorchestrator.service.AemHelperService;
import com.shinesolutions.swaggeraem4j.ApiException;

@Component
public class ScaleUpAuthorDispatcherAction implements ScaleAction {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private FlushAgentManager flushAgentManager;

    @Resource
    private AemHelperService aemHelperService;

    public boolean execute(String instanceId) {
        logger.info("ScaleUpAuthorDispatcherAction executing");
        boolean success = false;

        String authDispatcherAemBaseUrl = aemHelperService.getAemUrlForAuthorDispatcher(instanceId);

        String authElbAemBaseUrl = aemHelperService.getAemUrlForAuthorElb();

        try {
            logger.debug("Attempting to create flush agent at base AEM path: " + authElbAemBaseUrl);
            
            flushAgentManager.createFlushAgent(instanceId, authElbAemBaseUrl, authDispatcherAemBaseUrl,
                AgentRunMode.AUTHOR);
            success = true;
        } catch (ApiException e) {
            logger.error("Failed to create flush agent for dispatcher id: " + instanceId + ", and run mode: "
                + AgentRunMode.AUTHOR.getValue(), e);
        }

        return success;
    }

}
