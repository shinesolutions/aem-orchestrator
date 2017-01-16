package com.shinesolutions.aemorchestrator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shinesolutions.swaggeraem4j.ApiClient;

@Configuration
public class AemConfig {
    
    @Value("${aem.basePath}")
    private String basePath;
    
    @Value("${aem.username}")
    private String username;
    
    @Value("${aem.password}")
    private String password;

    
    @Bean
    public ApiClient apiClient() {
        ApiClient client = new ApiClient();
        client.setBasePath(basePath);
        client.setUsername(username);
        client.setPassword(password);
        client.setDebugging(false);
        return client;
    }
}
