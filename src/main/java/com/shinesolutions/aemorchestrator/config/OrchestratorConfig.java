package com.shinesolutions.aemorchestrator.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

@Configuration
public class OrchestratorConfig {
    
    @Value("${aem.relaxed.ssl.enable}")
    private Boolean enableRelaxedSSL;

    @Bean
    public ConversionService conversionService() {
        // Used for converting String to Collection types
        return new DefaultConversionService();
    }

    @Bean
    public HttpClient httpClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        HttpClient client;
        
        if(enableRelaxedSSL) {
            SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, (certificate, authType) -> true)
                .build();
    
            client = HttpClients.custom().setSSLContext(sslContext)
                .setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
        } else {
            client = HttpClientBuilder.create().build();
        }
        
        return client;
    }

}
