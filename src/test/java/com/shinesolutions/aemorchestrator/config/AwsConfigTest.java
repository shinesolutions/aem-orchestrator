package com.shinesolutions.aemorchestrator.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.shinesolutions.aemorchestrator.model.ProxyDetails;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.apache5.Apache5HttpClient;
import software.amazon.awssdk.http.apache5.ProxyConfiguration;

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

    int clientConnectionTimeout = 10;
    int clientMaxErrorRetry = 20;
    setField(awsConfig, "clientConnectionTimeout", clientConnectionTimeout);
    setField(awsConfig, "clientMaxErrorRetry", clientMaxErrorRetry);

    setField(awsConfig, "useProxy", false);
    ProxyConfiguration proxyConfig = awsConfig.proxyConfiguration(proxyDetails);
    ClientOverrideConfiguration overrideConfig = awsConfig.clientOverrideConfiguration();
    Apache5HttpClient.Builder httpClientBuilder = mock(Apache5HttpClient.Builder.class);
    awsConfig.configureHttpClient(httpClientBuilder, proxyDetails);

    assertThat(proxyConfig, nullValue());
    verify(httpClientBuilder, never()).proxyConfiguration(any(ProxyConfiguration.class));
    verify(httpClientBuilder).connectionTimeout(Duration.ofMillis(clientConnectionTimeout));
    verify(httpClientBuilder).socketTimeout(Duration.ofSeconds(50));
    assertThat(overrideConfig.retryStrategy().isPresent(), equalTo(true));
    assertThat(
        overrideConfig.retryStrategy().get().maxAttempts(), equalTo(clientMaxErrorRetry + 1));
  }

  @Test
  public void testAwsClientConfig_NoProxy() {
    int clientConnectionTimeout = 10;
    int clientMaxErrorRetry = 20;
    setField(awsConfig, "clientConnectionTimeout", clientConnectionTimeout);
    setField(awsConfig, "clientMaxErrorRetry", clientMaxErrorRetry);

    setField(awsConfig, "useProxy", false);
    ProxyConfiguration proxyConfig = awsConfig.proxyConfiguration(null);
    ClientOverrideConfiguration overrideConfig = awsConfig.clientOverrideConfiguration();
    Apache5HttpClient.Builder httpClientBuilder = mock(Apache5HttpClient.Builder.class);
    awsConfig.configureHttpClient(httpClientBuilder, null);

    assertThat(proxyConfig, nullValue());
    verify(httpClientBuilder, never()).proxyConfiguration(any(ProxyConfiguration.class));
    verify(httpClientBuilder).connectionTimeout(Duration.ofMillis(clientConnectionTimeout));
    verify(httpClientBuilder).socketTimeout(Duration.ofSeconds(50));
    assertThat(overrideConfig.retryStrategy().isPresent(), equalTo(true));
    assertThat(
        overrideConfig.retryStrategy().get().maxAttempts(), equalTo(clientMaxErrorRetry + 1));
  }

  @Test
  public void testAwsClientConfig_UseProxy() {
    int clientConnectionTimeout = 10;
    int clientMaxErrorRetry = 20;
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
    Apache5HttpClient.Builder httpClientBuilder = mock(Apache5HttpClient.Builder.class);
    awsConfig.configureHttpClient(httpClientBuilder, proxyDetails);

    assertThat(proxyConfig, notNullValue());
    assertThat(proxyConfig.host(), equalTo(clientProxyHost));
    assertThat(proxyConfig.port(), equalTo(clientProxyPort));
    assertThat(proxyConfig.scheme(), equalTo("http"));
    assertThat(configuredProxy(httpClientBuilder).host(), equalTo(proxyConfig.host()));
    assertThat(configuredProxy(httpClientBuilder).port(), equalTo(proxyConfig.port()));
    assertThat(configuredProxy(httpClientBuilder).scheme(), equalTo("http"));
    verify(httpClientBuilder).connectionTimeout(Duration.ofMillis(clientConnectionTimeout));
    verify(httpClientBuilder).socketTimeout(Duration.ofSeconds(50));
    assertThat(overrideConfig.retryStrategy().isPresent(), equalTo(true));
    assertThat(
        overrideConfig.retryStrategy().get().maxAttempts(), equalTo(clientMaxErrorRetry + 1));

    // 2. Assert HTTP Proxy branch
    setField(awsConfig, "useProxy", false);
    proxyConfig = awsConfig.proxyConfiguration(proxyDetails);
    overrideConfig = awsConfig.clientOverrideConfiguration();
    httpClientBuilder = mock(Apache5HttpClient.Builder.class);
    awsConfig.configureHttpClient(httpClientBuilder, proxyDetails);

    assertThat(proxyConfig, notNullValue());
    assertThat(proxyConfig.host(), equalTo(httpProxyHost));
    assertThat(proxyConfig.port(), equalTo(httpProxyPort));
    assertThat(proxyConfig.scheme(), equalTo("http"));
    assertThat(configuredProxy(httpClientBuilder).host(), equalTo(proxyConfig.host()));
    assertThat(configuredProxy(httpClientBuilder).port(), equalTo(proxyConfig.port()));
    assertThat(configuredProxy(httpClientBuilder).scheme(), equalTo("http"));
    verify(httpClientBuilder).connectionTimeout(Duration.ofMillis(clientConnectionTimeout));
    verify(httpClientBuilder).socketTimeout(Duration.ofSeconds(50));
    assertThat(overrideConfig.retryStrategy().isPresent(), equalTo(true));
    assertThat(
        overrideConfig.retryStrategy().get().maxAttempts(), equalTo(clientMaxErrorRetry + 1));
  }

  @Test
  public void testAwsCredentialsProvider() {
    AwsCredentialsProvider awsCredentialsProvider = awsConfig.awsCredentialsProvider();

    assertThat(awsCredentialsProvider, instanceOf(InstanceProfileCredentialsProvider.class));
  }

  /** ProxyConfiguration has no equals(), so capture what the HTTP client builder received. */
  private static ProxyConfiguration configuredProxy(Apache5HttpClient.Builder httpClientBuilder) {
    ArgumentCaptor<ProxyConfiguration> captor = ArgumentCaptor.forClass(ProxyConfiguration.class);
    verify(httpClientBuilder).proxyConfiguration(captor.capture());
    return captor.getValue();
  }
}
