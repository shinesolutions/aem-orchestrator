package com.shinesolutions.aemorchestrator.config;

import static org.mockito.Mockito.mock;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;

/*
 * Mock test configuration for AWS external dependencies
 */
@Configuration
@Profile("test")
public class MockAwsConfig {

    @Bean
    public SQSConnection getSqsConnection() throws JMSException {
        return mock(SQSConnection.class);
    }

    @Bean
    public MessageConsumer getSqsMessageConsumer(SQSConnection connection) throws JMSException {
        return new MockMessageConsumer();
    }
    
    @Bean
    public AmazonEC2 amazonEC2Client() {
        return mock(AmazonEC2.class);
    }
    
    @Bean
    public AmazonElasticLoadBalancing amazonElbClient() {
        return mock(AmazonElasticLoadBalancing.class);
    }
    
    @Bean 
    public AmazonAutoScaling amazonAutoScalingClient() {
        return mock(AmazonAutoScaling.class);
    }
    
    @Bean 
    public AmazonCloudFormation amazonCloudFormationClient() {
        return mock(AmazonCloudFormation.class);
    }

}
