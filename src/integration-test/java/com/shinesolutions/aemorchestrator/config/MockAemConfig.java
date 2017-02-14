package com.shinesolutions.aemorchestrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.shinesolutions.aemorchestrator.model.AemCredentials;
import com.shinesolutions.aemorchestrator.model.EnvironmentValues;
import com.shinesolutions.aemorchestrator.model.UserPasswordCredentials;

/*
 * Mock test configuration for AEM external dependencies
 */
@Configuration
@Profile("test")
public class MockAemConfig {
    
    @Bean
    public AemCredentials aemCredentials() {
        return new AemCredentials()
            .withOrchestratorCredentials(new UserPasswordCredentials()
                .withUserName("orchestrator_test")
                .withPassword("orchestrator_test")
                )
            .withReplicatorCredentials(new UserPasswordCredentials()
                .withUserName("replicator_test")
                .withPassword("replicator_test"));
    }
    
    @Bean
    public EnvironmentValues envValues() {
        EnvironmentValues envValues = new EnvironmentValues();
        
        envValues.setAutoScaleGroupNameForAuthorDispatcher("autoScaleGroupNameForAuthorDispatcher");
        envValues.setAutoScaleGroupNameForPublish("autoScaleGroupNameForPublish");
        envValues.setAutoScaleGroupNameForPublishDispatcher("autoScaleGroupNameForPublishDispatcher");
        envValues.setElasticLoadBalancerAuthorDns("elasticLoadBalancerAuthorDns");
        envValues.setElasticLoadBalancerNameForAuthor("elasticLoadBalancerNameForAuthor");
        
        return envValues;
    }

}
