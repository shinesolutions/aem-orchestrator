package com.shinesolutions.aemorchestrator.config;

import com.shinesolutions.aemorchestrator.model.ProxyDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.apache.ProxyConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class AwsConfigTest {
    
    private AwsConfig awsConfig;
    
    @BeforeEach
    public void setup() {
        awsConfig = new AwsConfig();
    }
    
    @Test
    public void testAwsClientConfig_EmptyProxy() {
        // Setup HTTP proxy
        String httpProxyHost = "";
        ProxyDetails proxyDetails = new ProxyDetails();
        proxyDetails.setHost(httpProxyHost);

        String clientProtocol = "http";
        int clientConnectionTimeout = 10;
        int clientMaxErrorRetry = 20;
        setField(awsConfig, "clientProtocol", clientProtocol);
        setField(awsConfig, "clientConnectionTimeout", clientConnectionTimeout);
        setField(awsConfig, "clientMaxErrorRetry", clientMaxErrorRetry);

        setField(awsConfig, "useProxy", false);
        ProxyConfiguration proxyConfig = awsConfig.proxyConfiguration(proxyDetails);
        ClientOverrideConfiguration overrideConfig = awsConfig.clientOverrideConfiguration();

        assertThat(proxyConfig, nullValue());
        assertThat(overrideConfig.retryPolicy().isPresent(), equalTo(true));
        assertThat(overrideConfig.retryPolicy().get().numRetries(), equalTo(clientMaxErrorRetry));
    }

    @Test
    public void testAwsClientConfig_NoProxy() {
        String clientProtocol = "http";
        int clientConnectionTimeout = 10;
        int clientMaxErrorRetry = 20;
        setField(awsConfig, "clientProtocol", clientProtocol);
        setField(awsConfig, "clientConnectionTimeout", clientConnectionTimeout);
        setField(awsConfig, "clientMaxErrorRetry", clientMaxErrorRetry);
        
        setField(awsConfig, "useProxy", false);
        ProxyConfiguration proxyConfig = awsConfig.proxyConfiguration(null);
        ClientOverrideConfiguration overrideConfig = awsConfig.clientOverrideConfiguration();
        
        assertThat(proxyConfig, nullValue());
        assertThat(overrideConfig.retryPolicy().isPresent(), equalTo(true));
        assertThat(overrideConfig.retryPolicy().get().numRetries(), equalTo(clientMaxErrorRetry));
    }
    
    @Test
    public void testAwsClientConfig_UseProxy() {
        String clientProtocol = "http";
        int clientConnectionTimeout = 10;
        int clientMaxErrorRetry = 20;
        setField(awsConfig, "clientProtocol", clientProtocol);
        setField(awsConfig, "clientConnectionTimeout", clientConnectionTimeout);
        setField(awsConfig, "clientMaxErrorRetry", clientMaxErrorRetry);
        
        // Setup client proxy
        String clientProxyHost = "clientProxyHost";
        Integer clientProxyPort = 1;
        setField(awsConfig, "clientProxyHost", clientProxyHost);
        setField(awsConfig, "clientProxyPort", clientProxyPort);
        
        // Setup HTTP proxy
        String httpProxyHost = "httpProxyHost";
        Integer httpProxyPort = 2;
        ProxyDetails proxyDetails = new ProxyDetails();
        proxyDetails.setHost(httpProxyHost);
        proxyDetails.setPort(httpProxyPort);
        
        // 1. Assert Client Proxy branch
        setField(awsConfig, "useProxy", true);
        ProxyConfiguration proxyConfig = awsConfig.proxyConfiguration(proxyDetails);
        ClientOverrideConfiguration overrideConfig = awsConfig.clientOverrideConfiguration();
        
        assertThat(proxyConfig, notNullValue());
        assertThat(proxyConfig.host(), equalTo(clientProxyHost));
        assertThat(proxyConfig.port(), equalTo(clientProxyPort));
        assertThat(proxyConfig.scheme(), equalTo(clientProtocol));
        assertThat(overrideConfig.retryPolicy().isPresent(), equalTo(true));
        assertThat(overrideConfig.retryPolicy().get().numRetries(), equalTo(clientMaxErrorRetry));
        
        // 2. Assert HTTP Proxy branch
        setField(awsConfig, "useProxy", false);
        proxyConfig = awsConfig.proxyConfiguration(proxyDetails);
        overrideConfig = awsConfig.clientOverrideConfiguration();
        
        assertThat(proxyConfig, notNullValue());
        assertThat(proxyConfig.host(), equalTo(httpProxyHost));
        assertThat(proxyConfig.port(), equalTo(httpProxyPort));
        assertThat(proxyConfig.scheme(), equalTo(clientProtocol));
        assertThat(overrideConfig.retryPolicy().isPresent(), equalTo(true));
        assertThat(overrideConfig.retryPolicy().get().numRetries(), equalTo(clientMaxErrorRetry));
    }
    
    @Test
    public void testAwsCredentialsProvider() {
        AwsCredentialsProvider awsCredentialsProvider = awsConfig.awsCredentialsProvider();
        
        assertThat(awsCredentialsProvider, notNullValue());
    }
}