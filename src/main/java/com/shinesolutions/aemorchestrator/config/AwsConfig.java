package com.shinesolutions.aemorchestrator.config;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazon.sqs.javamessaging.SQSSession;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClientBuilder;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.util.EC2MetadataUtils;
import com.shinesolutions.aemorchestrator.model.ProxyDetails;

@Configuration
@Profile("default")
public class AwsConfig {

    @Value("${aws.sqs.queueName}")
    private String queueName;

    @Value("${aws.client.useProxy}")
    private Boolean useProxy;

    @Value("${aws.client.protocol}")
    private String clientProtocol;

    @Value("${aws.client.proxy.host}")
    private String clientProxyHost;

    @Value("${aws.client.proxy.port}")
    private Integer clientProxyPort;

    @Value("${aws.client.connection.timeout}")
    private Integer clientConnectionTimeout;

    @Value("${aws.client.max.errorRetry}")
    private Integer clientMaxErrorRetry;

    
    @Bean
    public AWSCredentialsProvider awsCredentialsProvider() {
        /*
         * For info on how this works, see:
         * http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/
         * credentials.html
         */
        return new DefaultAWSCredentialsProviderChain();
    }

    @Bean
    public SQSConnection sqsConnection(AWSCredentialsProvider awsCredentialsProvider,
        ClientConfiguration awsClientConfig) throws JMSException {
        
        SQSConnectionFactory connectionFactory = SQSConnectionFactory.builder()
            .withRegion(RegionUtils.getRegion(EC2MetadataUtils.getEC2InstanceRegion())) //Gets region form meta data
            .withAWSCredentialsProvider(awsCredentialsProvider)
            .withClientConfiguration(awsClientConfig)
            .build();

        return connectionFactory.createConnection();
    }

    @Bean
    public MessageConsumer sqsMessageConsumer(SQSConnection connection) throws JMSException {

        /*
         * Create the session and use UNORDERED_ACKNOWLEDGE mode. Acknowledging
         * messages deletes them from the queue. Each message must be individually
         * acknowledged
         */
        Session session = connection.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);

        return session.createConsumer(session.createQueue(queueName));
    }

    @Bean
    public ClientConfiguration awsClientConfig(final ProxyDetails proxyDetails) {
        ClientConfiguration clientConfig = new ClientConfiguration();

        if (useProxy) {
            clientConfig.setProxyHost(clientProxyHost);
            clientConfig.setProxyPort(clientProxyPort);
        } else if(proxyDetails != null) {
            clientConfig.setProxyHost(proxyDetails.getHost());
            clientConfig.setProxyPort(proxyDetails.getPort());
        }

        clientConfig.setProtocol(Protocol.valueOf(clientProtocol.toUpperCase()));
        clientConfig.setConnectionTimeout(clientConnectionTimeout);
        clientConfig.setMaxErrorRetry(clientMaxErrorRetry);

        return clientConfig;
    }

    @Bean
    public AmazonEC2 amazonEC2Client(AWSCredentialsProvider awsCredentialsProvider,
        ClientConfiguration awsClientConfig) {
        return AmazonEC2ClientBuilder.standard()
            .withCredentials(awsCredentialsProvider)
            .withClientConfiguration(awsClientConfig)
            .build();
    }

    @Bean
    public AmazonElasticLoadBalancing amazonElbClient(AWSCredentialsProvider awsCredentialsProvider,
        ClientConfiguration awsClientConfig) {
        return AmazonElasticLoadBalancingClientBuilder.standard()
            .withCredentials(awsCredentialsProvider)
            .withClientConfiguration(awsClientConfig)
            .build();
    }

    @Bean
    public AmazonAutoScaling amazonAutoScalingClient(AWSCredentialsProvider awsCredentialsProvider,
        ClientConfiguration awsClientConfig) {
        return AmazonAutoScalingClientBuilder.standard()
            .withCredentials(awsCredentialsProvider)
            .withClientConfiguration(awsClientConfig)
            .build();
    }

    @Bean
    public AmazonCloudFormation amazonCloudFormationClient(AWSCredentialsProvider awsCredentialsProvider,
        ClientConfiguration awsClientConfig) {
        return AmazonCloudFormationClientBuilder.standard()
            .withCredentials(awsCredentialsProvider)
            .withClientConfiguration(awsClientConfig)
            .build();
    }
    
    @Bean
    public AmazonS3 amazonS3Client(AWSCredentialsProvider awsCredentialsProvider, ClientConfiguration awsClientConfig) {
        return AmazonS3ClientBuilder.standard()
            .withCredentials(awsCredentialsProvider)
            .withClientConfiguration(awsClientConfig)
            .build();
    }

}
