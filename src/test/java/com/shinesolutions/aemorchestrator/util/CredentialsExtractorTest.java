package com.shinesolutions.aemorchestrator.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.util.Scanner;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinesolutions.aemorchestrator.model.AemCredentials;

public class CredentialsExtractorTest {

    
    @Test
    @SuppressWarnings("resource")
    public void testExtractSuccess() throws Exception {
        //Read file into a string
        File sampleFile = new File(getClass().getResource("/sample-aem-credentials.json").getFile());
        String sampleFileContent = new Scanner(sampleFile).useDelimiter("\\Z").next();
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(sampleFileContent);
        
        AemCredentials aemCredentials = CredentialsExtractor.extractAemCredentials(sampleFileContent);
        
        assertThat(aemCredentials.getOrchestratorCredentials().getUserName(), 
            equalTo(CredentialsExtractor.ORCHESTRATOR_USER));
        assertThat(aemCredentials.getOrchestratorCredentials().getPassword(), 
            equalTo(root.path(CredentialsExtractor.ORCHESTRATOR_USER).asText()));
        
        assertThat(aemCredentials.getReplicatorCredentials().getUserName(), 
            equalTo(CredentialsExtractor.REPLICATOR_USER));
        assertThat(aemCredentials.getReplicatorCredentials().getPassword(), 
            equalTo(root.path(CredentialsExtractor.REPLICATOR_USER).asText()));
    }

}
