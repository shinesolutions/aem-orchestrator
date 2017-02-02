package com.shinesolutions.aemorchestrator.aem;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.model.AemCredentials;
import com.shinesolutions.swaggeraem4j.ApiClient;
import com.shinesolutions.swaggeraem4j.api.SlingApi;

@Component
public class AemApiFactory {

    @Value("${aem.client.api.debug}")
    private Boolean useDebug;
    
    @Value("${aem.client.api.connection.timeout}")
    private Integer connectionTimeout;
    
    @Resource
    private AemCredentials aemCredentials;
    
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
        client.setDebugging(useDebug);
        client.setConnectTimeout(connectionTimeout);
        
        if(user == UserType.REPLICATOR) {
            client.setUsername(aemCredentials.getReplicatorCredentials().getUserName());
            client.setPassword(aemCredentials.getReplicatorCredentials().getPassword());
        } 
        else { //Default is ORCHESTRATOR
            client.setUsername(aemCredentials.getOrchestratorCredentials().getUserName());
            client.setPassword(aemCredentials.getOrchestratorCredentials().getPassword());
        }
        
        return client;
    }

}
