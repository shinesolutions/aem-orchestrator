package com.shinesolutions.aemorchestrator.config;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.shinesolutions.swaggeraem4j.ApiClient;
import com.shinesolutions.swaggeraem4j.api.SlingApi;

/*
 * Mock test configuration for AEM external dependencies
 */
@Configuration
@Profile("test")
public class MockAemConfig {
    
    @Bean
    public ApiClient apiClient() {
        return mock(ApiClient.class);
    }
    
    @Bean
    public SlingApi slingApi(ApiClient apiClient) {
        return mock(SlingApi.class);
    }

}
