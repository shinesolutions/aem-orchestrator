package com.shinesolutions.aemorchestrator.aem;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    
    @Value("${aem.swaggeraem4j.useDebugging}")
    private Boolean useApiClientDebugging;
    
    private static enum UserType {
        ORCHESTRATOR,
        REPLICATOR
    };
    
    public SlingApi getSlingApi(String baseBath, AgentAction action) {
        //Only a 'create' type agent action requires the replicator user
        UserType user = action == AgentAction.CREATE ? UserType.REPLICATOR : UserType.ORCHESTRATOR;

        return new SlingApi(getApiClient(baseBath, user));
    }
    
    private ApiClient getApiClient(String basePath, UserType user) {
        ApiClient client = new ApiClient();
        
        client.setBasePath(basePath);
        client.setDebugging(useApiClientDebugging);
        
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
