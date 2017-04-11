package com.shinesolutions.aemorchestrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

@Configuration
public class OrchestratorConfig {
    
    @Bean
    public ConversionService conversionService() {
        // Used for converting String to Collection types
        return new DefaultConversionService();
    }
}
