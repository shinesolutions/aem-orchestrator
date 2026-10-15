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
import software.amazon.awssdk.awscore.retry.AwsRetryStrategy;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
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

/**
 * Spring configuration, active in the {@code default} profile, for the AWS SDK v2 clients and the
 * SQS JMS consumer that receives orchestration events.
 *
 * <p>All service clients share the same credentials provider, region, HTTP client and override
 * configuration, configured by the {@code aws.*} properties.
 */
@Configuration
@Profile("default")
public class AwsConfig {

  @Value("${aws.region:@null}")
  private String regionString;

  @Value("${aws.sqs.queueName}")
  private String queueName;

  // No defaults for aws.client.*: a missing property fails at startup, as it did with SDK v1,
  // rather
  // than silently running without a proxy, timeout or retry limit
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

  /** Provides credentials from the EC2 instance profile the orchestrator runs under. */
  @Bean
  public AwsCredentialsProvider awsCredentialsProvider() {
    // Instance profile only, as with SDK v1. SDK v2's DefaultCredentialsProvider would let
    // environment variables or ~/.aws credentials on the host take precedence over the EC2 role
    return InstanceProfileCredentialsProvider.create();
  }

  /**
   * Provides the AWS region used by all clients.
   *
   * <p>Uses {@code aws.region} when set, otherwise the SDK default region provider chain.
   *
   * @throws BeanInitializationException if the region is unknown or cannot be determined
   */
  @Bean
  public Region awsRegion() {
    Region region;
    if (regionString != null && !regionString.isEmpty()) {
      // Unknown region names resolve to null, as RegionUtils.getRegion did in SDK v1. Region.of()
      // accepts any string, so a typo would otherwise only fail at the first AWS call
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
      // SDK v1, and SDK v2 always calls AWS over HTTPS. port.intValue() fails fast when
      // aws.client.useProxy is true without aws.client.proxy.port, as SDK v1 did
      return ProxyConfiguration.builder()
          .endpoint(URI.create("http://" + host + ":" + port.intValue()))
          .build();
    }
    return null;
  }

  /**
   * Provides the Apache HTTP client shared by all AWS clients.
   *
   * <p>The connection timeout is {@code aws.client.connection.timeout} milliseconds and the socket
   * timeout is 50 seconds. A proxy is used when {@code aws.client.useProxy} is true ({@code
   * aws.client.proxy.host} and {@code aws.client.proxy.port}), or otherwise when {@code
   * proxyDetails} has a host, as detected from {@code https_proxy} by {@link ProxyConfig}.
   */
  @Bean
  public SdkHttpClient httpClient(@Nullable final ProxyDetails proxyDetails) {
    // SDK v2 transport on HttpClient 5 (apache5-client), so HttpClient 4 is no longer used. One
    // client and connection pool is shared by all AWS clients; SDK v1 created one per client
    return configureHttpClient(Apache5HttpClient.builder(), proxyDetails).build();
  }

  // Package-private so AwsConfigTest can check the timeouts and proxy on a mock builder: the built
  // SDK v2 client has no getters for them
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

  /**
   * Provides the override configuration shared by all AWS clients, which applies the legacy retry
   * strategy with up to {@code aws.client.max.errorRetry} retries after the first attempt.
   */
  @Bean
  public ClientOverrideConfiguration clientOverrideConfiguration() {
    ClientOverrideConfiguration.Builder builder = ClientOverrideConfiguration.builder();

    // Legacy retry mode matches SDK v1 backoff; maxAttempts counts the first attempt. Replaces the
    // deprecated RetryPolicy.numRetries(clientMaxErrorRetry) with the same number of retries
    builder.retryStrategy(
        AwsRetryStrategy.legacyRetryStrategy().toBuilder()
            .maxAttempts(clientMaxErrorRetry + 1)
            .build());

    return builder.build();
  }

  /**
   * Provides the SQS client used by the JMS connection, configured with the shared AWS client
   * settings.
   */
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

  /**
   * Provides the JMS connection to SQS, created with the default provider configuration.
   *
   * @throws JMSException if the connection cannot be created
   */
  @Bean
  public SQSConnection sqsConnection(final SqsClient sqsClient) throws JMSException {
    // amazon-sqs-java-messaging-lib 2.x takes the SDK v2 SqsClient, so the JMS connection uses the
    // same region, credentials, proxy, timeouts and retries as the other AWS clients
    SQSConnectionFactory connectionFactory =
        new SQSConnectionFactory(new ProviderConfiguration(), sqsClient);

    return connectionFactory.createConnection();
  }

  /**
   * Provides the JMS consumer for the {@code aws.sqs.queueName} queue, using a non-transacted
   * session in unordered acknowledge mode so that each message is acknowledged individually.
   *
   * @throws JMSException if the session or consumer cannot be created
   */
  @Bean
  public MessageConsumer sqsMessageConsumer(final SQSConnection connection) throws JMSException {
    Session session = connection.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);
    return session.createConsumer(session.createQueue(queueName));
  }

  /** Provides the EC2 client, configured with the shared AWS client settings. */
  @Bean
  public Ec2Client amazonEc2Client(
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

  /**
   * Provides the Elastic Load Balancing v2 client, configured with the shared AWS client settings.
   */
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

  /** Provides the Auto Scaling client, configured with the shared AWS client settings. */
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

  /** Provides the CloudFormation client, configured with the shared AWS client settings. */
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

  /** Provides the S3 client, configured with the shared AWS client settings. */
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

  /** Provides the CloudWatch client, configured with the shared AWS client settings. */
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
