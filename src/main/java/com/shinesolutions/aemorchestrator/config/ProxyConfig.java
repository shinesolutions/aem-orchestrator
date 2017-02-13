package com.shinesolutions.aemorchestrator.config;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shinesolutions.aemorchestrator.model.ProxyDetails;

@Configuration
public class ProxyConfig {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private static final String ENV_HTTPS_PROXY = "https_proxy";
    
    @Bean
    public ProxyDetails proxyDetails() {
        ProxyDetails details = null;
        
        String httpsProxyEnvironmentVar = System.getenv(ENV_HTTPS_PROXY);
        
        if(httpsProxyEnvironmentVar != null && !httpsProxyEnvironmentVar.isEmpty()) {
            logger.debug(ENV_HTTPS_PROXY + " env variable detected: " + httpsProxyEnvironmentVar);
            try {
                URL httpProxyUrl = new URL(httpsProxyEnvironmentVar);
                details = new ProxyDetails()
                    .withHost(httpProxyUrl.getHost())
                    .withPort(httpProxyUrl.getPort());
                logger.debug("Proxy details set to host: " + details.getHost() + ", port: " + details.getPort());
            } catch (MalformedURLException e) {
                logger.warn("Unable to parse " + ENV_HTTPS_PROXY + " environment variable", e);
            }
        }
        
        return details;
    }

}
