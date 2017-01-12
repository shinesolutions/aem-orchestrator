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
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;

@Configuration
@Profile("default")
public class AwsSqsConfig {

    @Value("${aws.sqs.queueName}")
    private String queueName;

    @Value("${aws.region}")
    private String regionString;

    @Bean
    public SQSConnection getSqsConnection() throws JMSException {

        Region region = RegionUtils.getRegion(regionString);

        /*
         * For info on how this works, see:
         * http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/
         * credentials.html
         */
        AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();

        SQSConnectionFactory connectionFactory = SQSConnectionFactory.builder().withRegion(region)
            .withAWSCredentialsProvider(credentialsProvider).build();

        // Create the connection
        SQSConnection connection = connectionFactory.createConnection();

        return connection;
    }

    @Bean
    public MessageConsumer getSqsMessageConsumer(SQSConnection connection) throws JMSException {

        /*
         * Create the session and use CLIENT_ACKNOWLEDGE mode. Acknowledging
         * messages deletes them from the queue
         */
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(session.createQueue(queueName));

        return consumer;
    }

}
