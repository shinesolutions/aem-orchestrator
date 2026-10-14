package com.shinesolutions.aemorchestrator.config;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazon.sqs.javamessaging.SQSSession;
import com.shinesolutions.aemorchestrator.model.ProxyDetails;
import jakarta.annotation.Nullable;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import java.net.URI;
import java.time.Duration;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache5.Apache5HttpClient;
import software.amazon.awssdk.http.apache5.ProxyConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@Profile("default")
public class AwsConfig {

  @Value("${aws.region:@null}")
  private String regionString;

  @Value("${aws.sqs.queueName}")
  private String queueName;

  @Value("${aws.client.useProxy}")
  private Boolean useProxy;

  @Value("${aws.client.proxy.host}")
  private String clientProxyHost;

  @Value("${aws.client.proxy.port}")
  private Integer clientProxyPort;

  @Value("${aws.client.connection.timeout}")
  private Integer clientConnectionTimeout;

  @Value("${aws.client.max.errorRetry}")
  private Integer clientMaxErrorRetry;

  // SDK v1 ClientConfiguration default; SDK v2's Apache client defaults to 30 seconds
  private static final Duration SOCKET_TIMEOUT = Duration.ofSeconds(50);

  @Bean
  public AwsCredentialsProvider awsCredentialsProvider() {
    return InstanceProfileCredentialsProvider.create();
  }

  @Bean
  public Region awsRegion() {
    Region region;
    if (regionString != null && !regionString.isEmpty()) {
      // Unknown region names resolve to null, as RegionUtils.getRegion did in SDK v1
      region =
          Region.regions().stream()
              .filter(r -> r.id().equals(regionString))
              .findFirst()
              .orElse(null);
    } else {
      region = DefaultAwsRegionProviderChain.builder().build().getRegion();
    }

    if (region == null) {
      throw new BeanInitializationException("Unable to determine AWS region");
    }

    return region;
  }

  ProxyConfiguration proxyConfiguration(@Nullable final ProxyDetails proxyDetails) {
    String host = null;
    Integer port = null;

    if (useProxy) {
      host = clientProxyHost;
      port = clientProxyPort;
    } else if (proxyDetails != null
        && proxyDetails.getHost() != null
        && !proxyDetails.getHost().isEmpty()) {
      host = proxyDetails.getHost();
      port = proxyDetails.getPort();
    }

    if (host != null && !host.isEmpty()) {
      // Proxies are reached over plain HTTP; aws.client.protocol only applied to AWS endpoints in
      // SDK v1, and SDK v2 always calls AWS over HTTPS
      return ProxyConfiguration.builder()
          .endpoint(URI.create("http://" + host + ":" + port.intValue()))
          .build();
    }
    return null;
  }

  @Bean
  public SdkHttpClient httpClient(@Nullable final ProxyDetails proxyDetails) {
    return configureHttpClient(Apache5HttpClient.builder(), proxyDetails).build();
  }

  Apache5HttpClient.Builder configureHttpClient(
      final Apache5HttpClient.Builder builder, @Nullable final ProxyDetails proxyDetails) {
    ProxyConfiguration proxyConfig = proxyConfiguration(proxyDetails);
    if (proxyConfig != null) {
      builder.proxyConfiguration(proxyConfig);
    }

    builder.connectionTimeout(Duration.ofMillis(clientConnectionTimeout));
    builder.socketTimeout(SOCKET_TIMEOUT);

    return builder;
  }

  @Bean
  public ClientOverrideConfiguration clientOverrideConfiguration() {
    ClientOverrideConfiguration.Builder builder = ClientOverrideConfiguration.builder();

    builder.retryPolicy(RetryPolicy.builder().numRetries(clientMaxErrorRetry).build());

    return builder.build();
  }

  @Bean
  public SqsClient sqsClient(
      final AwsCredentialsProvider awsCredentialsProvider,
      final SdkHttpClient httpClient,
      final ClientOverrideConfiguration overrideConfig,
      final Region awsRegion) {

    return SqsClient.builder()
        .credentialsProvider(awsCredentialsProvider)
        .httpClient(httpClient)
        .overrideConfiguration(overrideConfig)
        .region(awsRegion)
        .build();
  }

  @Bean
  public SQSConnection sqsConnection(final SqsClient sqsClient) throws JMSException {
    SQSConnectionFactory connectionFactory =
        new SQSConnectionFactory(new ProviderConfiguration(), sqsClient);

    return connectionFactory.createConnection();
  }

  @Bean
  public MessageConsumer sqsMessageConsumer(final SQSConnection connection) throws JMSException {
    Session session = connection.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);
    return session.createConsumer(session.createQueue(queueName));
  }

  @Bean
  public Ec2Client amazonEC2Client(
      final AwsCredentialsProvider awsCredentialsProvider,
      final SdkHttpClient httpClient,
      final ClientOverrideConfiguration overrideConfig,
      final Region awsRegion) {

    return Ec2Client.builder()
        .credentialsProvider(awsCredentialsProvider)
        .httpClient(httpClient)
        .overrideConfiguration(overrideConfig)
        .region(awsRegion)
        .build();
  }

  @Bean
  public ElasticLoadBalancingV2Client amazonElbClient(
      final AwsCredentialsProvider awsCredentialsProvider,
      final SdkHttpClient httpClient,
      final ClientOverrideConfiguration overrideConfig,
      final Region awsRegion) {

    return ElasticLoadBalancingV2Client.builder()
        .credentialsProvider(awsCredentialsProvider)
        .httpClient(httpClient)
        .overrideConfiguration(overrideConfig)
        .region(awsRegion)
        .build();
  }

  @Bean
  public AutoScalingClient amazonAutoScalingClient(
      final AwsCredentialsProvider awsCredentialsProvider,
      final SdkHttpClient httpClient,
      final ClientOverrideConfiguration overrideConfig,
      final Region awsRegion) {

    return AutoScalingClient.builder()
        .credentialsProvider(awsCredentialsProvider)
        .httpClient(httpClient)
        .overrideConfiguration(overrideConfig)
        .region(awsRegion)
        .build();
  }

  @Bean
  public CloudFormationClient amazonCloudFormationClient(
      final AwsCredentialsProvider awsCredentialsProvider,
      final SdkHttpClient httpClient,
      final ClientOverrideConfiguration overrideConfig,
      final Region awsRegion) {

    return CloudFormationClient.builder()
        .credentialsProvider(awsCredentialsProvider)
        .httpClient(httpClient)
        .overrideConfiguration(overrideConfig)
        .region(awsRegion)
        .build();
  }

  @Bean
  public S3Client amazonS3Client(
      final AwsCredentialsProvider awsCredentialsProvider,
      final SdkHttpClient httpClient,
      final ClientOverrideConfiguration overrideConfig,
      final Region awsRegion) {

    return S3Client.builder()
        .credentialsProvider(awsCredentialsProvider)
        .httpClient(httpClient)
        .overrideConfiguration(overrideConfig)
        .region(awsRegion)
        .build();
  }

  @Bean
  public CloudWatchClient amazonCloudWatchClient(
      final AwsCredentialsProvider awsCredentialsProvider,
      final SdkHttpClient httpClient,
      final ClientOverrideConfiguration overrideConfig,
      final Region awsRegion) {

    return CloudWatchClient.builder()
        .credentialsProvider(awsCredentialsProvider)
        .httpClient(httpClient)
        .overrideConfiguration(overrideConfig)
        .region(awsRegion)
        .build();
  }
}
