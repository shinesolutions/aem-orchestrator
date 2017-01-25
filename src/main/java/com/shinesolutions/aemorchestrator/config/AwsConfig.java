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
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;

@Configuration
@Profile("default")
public class AwsConfig {

    @Value("${aws.sqs.queueName}")
    private String queueName;

    @Value("${aws.region}")
    private String regionString;
    
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
    public Region awsRegion() {
        return RegionUtils.getRegion(regionString);
    }

    @Bean
    public SQSConnection sqsConnection(AWSCredentialsProvider awsCredentialsProvider, Region awsRegion) throws JMSException {

        SQSConnectionFactory connectionFactory = SQSConnectionFactory.builder()
            .withRegion(awsRegion)
            .withAWSCredentialsProvider(awsCredentialsProvider).build();

        // Create the connection
        SQSConnection connection = connectionFactory.createConnection();

        return connection;
    }

    @Bean
    public MessageConsumer sqsMessageConsumer(SQSConnection connection) throws JMSException {

        /*
         * Create the session and use CLIENT_ACKNOWLEDGE mode. Acknowledging
         * messages deletes them from the queue
         */
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(session.createQueue(queueName));

        return consumer;
    }
    
    @Bean
    public ClientConfiguration awsClientConfig() {
        return new ClientConfiguration()
            .withProtocol(Protocol.valueOf(clientProtocol.toUpperCase()))
            .withProxyHost(clientProxyHost)
            .withProxyPort(clientProxyPort)
            .withConnectionTimeout(clientConnectionTimeout)
            .withMaxErrorRetry(clientMaxErrorRetry);
    }
    
    @Bean
    public AmazonEC2 amazonEC2Client(AWSCredentialsProvider awsCredentialsProvider, 
        ClientConfiguration awsClientConfig) {
        return new AmazonEC2Client(awsCredentialsProvider, awsClientConfig);
    }
    
    @Bean
    public AmazonElasticLoadBalancing amazonElbClient(AWSCredentialsProvider awsCredentialsProvider, 
        ClientConfiguration awsClientConfig) {
        return new AmazonElasticLoadBalancingClient(awsCredentialsProvider, awsClientConfig);
    }
    
    @Bean 
    public AmazonAutoScaling amazonAutoScalingClient(AWSCredentialsProvider awsCredentialsProvider, 
        ClientConfiguration awsClientConfig) {
        return new AmazonAutoScalingClient(awsCredentialsProvider, awsClientConfig);
    }
    
    @Bean 
    public AmazonCloudFormation amazonCloudFormationClient(AWSCredentialsProvider awsCredentialsProvider, 
        ClientConfiguration awsClientConfig) {
        return new AmazonCloudFormationClient(awsCredentialsProvider, awsClientConfig);
    }

}
