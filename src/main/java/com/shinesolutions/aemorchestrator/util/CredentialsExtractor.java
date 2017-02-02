package com.shinesolutions.aemorchestrator.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinesolutions.aemorchestrator.model.AemCredentials;
import com.shinesolutions.aemorchestrator.model.UserPasswordCredentials;

public class CredentialsExtractor {
    
    public static final String REPLICATOR_USER = "replicator";
    public static final String ORCHESTRATOR_USER = "orchestrator";
    
    public static AemCredentials extractAemCredentials(String fileContents) 
        throws JsonProcessingException, IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(fileContents);
        
        AemCredentials aemCredentials = new AemCredentials();
        aemCredentials.setOrchestratorCredentials(new UserPasswordCredentials()
                .withUserName(ORCHESTRATOR_USER)
                .withPassword(root.path(ORCHESTRATOR_USER).asText()));
        
        aemCredentials.setReplicatorCredentials(new UserPasswordCredentials()
                .withUserName(REPLICATOR_USER)
                .withPassword(root.path(REPLICATOR_USER).asText()));
        
        return aemCredentials;
    }

}
