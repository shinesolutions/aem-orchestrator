package com.shinesolutions.aemorchestrator.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.shinesolutions.aemorchestrator.model.ProxyDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class AwsConfigTest {

    private AwsConfig awsConfig;

    @Before
    public void setup() {
        awsConfig = new AwsConfig();
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
        ClientConfiguration clientConfiguration = awsConfig.awsClientConfig(null);

        assertThat(clientConfiguration.getProxyHost(), nullValue());
        assertThat(clientConfiguration.getProxyPort(), equalTo(-1));
        assertThat(clientConfiguration.getProtocol().toString(), equalTo(clientProtocol));
        assertThat(clientConfiguration.getConnectionTimeout(), equalTo(clientConnectionTimeout));
        assertThat(clientConfiguration.getMaxErrorRetry(), equalTo(clientMaxErrorRetry));
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

        // Use client proxy
        setField(awsConfig, "useProxy", true);
        ClientConfiguration clientConfiguration = awsConfig.awsClientConfig(proxyDetails);

        assertThat(clientConfiguration.getProxyHost(), equalTo(clientProxyHost));
        assertThat(clientConfiguration.getProxyPort(), equalTo(clientProxyPort));
        assertThat(clientConfiguration.getProtocol().toString(), equalTo(clientProtocol));
        assertThat(clientConfiguration.getConnectionTimeout(), equalTo(clientConnectionTimeout));
        assertThat(clientConfiguration.getMaxErrorRetry(), equalTo(clientMaxErrorRetry));

        // Use HTTP proxy
        setField(awsConfig, "useProxy", false);
        clientConfiguration = awsConfig.awsClientConfig(proxyDetails);

        assertThat(clientConfiguration.getProxyHost(), equalTo(httpProxyHost));
        assertThat(clientConfiguration.getProxyPort(), equalTo(httpProxyPort));
        assertThat(clientConfiguration.getProtocol().toString(), equalTo(clientProtocol));
        assertThat(clientConfiguration.getConnectionTimeout(), equalTo(clientConnectionTimeout));
        assertThat(clientConfiguration.getMaxErrorRetry(), equalTo(clientMaxErrorRetry));
    }

    @Test
    public void testAwsCredentialsProvider() {
        AWSCredentialsProvider awsCredentialsProvider = awsConfig.awsCredentialsProvider();

        assertThat(awsCredentialsProvider, notNullValue());
    }
}
