package com.shinesolutions.aemorchestrator.aem;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.service.AemLookupService;
import com.shinesolutions.swaggeraem4j.ApiClient;
import com.shinesolutions.swaggeraem4j.api.SlingApi;

@Component
public class AemApiFactory {
    
    @Value("${aem.replicator.username}")
    private String replicatorUsername;
    
    @Value("${aem.replicator.password}")
    private String replicatorPassword;
    
    @Value("${aem.orchestrator.username}")
    private String orchestratorUsername;
    
    @Value("${aem.orchestrator.password}")
    private String orchestratorPassword;
    
    @Resource
    private AemLookupService aemLookupService;
    
    private static enum UserType {
        ORCHESTRATOR,
        REPLICATOR
    };
    
    public SlingApi getSlingApi(AgentRunMode runMode, AgentAction action, String instanceId) {
        String baseBath = null;
        
        //Only a create type agent action uses the replicator user
        UserType user = action == AgentAction.CREATE ? UserType.REPLICATOR : UserType.ORCHESTRATOR;
        
        if(runMode == AgentRunMode.AUTHOR) {
            baseBath = aemLookupService.getAemUrlForAuthorDispatcher();
        } 
        else if (runMode == AgentRunMode.PUBLISH) { //Must be PUBLISHER
            baseBath = aemLookupService.getAemUrlForPublisher(instanceId);
        }

        return new SlingApi(getApiClient(baseBath, user));
    }
    
    private ApiClient getApiClient(String basePath, UserType user) {
        ApiClient client = new ApiClient();
        
        client.setBasePath(basePath);
        client.setDebugging(false);
        
        if(user == UserType.REPLICATOR) {
            client.setUsername(replicatorUsername);
            client.setPassword(replicatorPassword);
        } 
        else { //Default is ORCHESTRATOR
            client.setUsername(orchestratorUsername);
            client.setPassword(orchestratorPassword);
        }
        
        return client;
    }

}
