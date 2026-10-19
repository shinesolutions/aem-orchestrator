package com.shinesolutions.aemorchestrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

/** Spring configuration for general-purpose orchestrator beans. */
@Configuration
public class OrchestratorConfig {

  /**
   * Provides the default conversion service, used to convert String property values to collection
   * types.
   */
  @Bean
  public ConversionService conversionService() {
    // Used for converting String to Collection types
    return new DefaultConversionService();
  }
}
