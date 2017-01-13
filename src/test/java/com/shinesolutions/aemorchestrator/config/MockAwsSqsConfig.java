package com.shinesolutions.aemorchestrator.config;

import static org.mockito.Mockito.mock;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazon.sqs.javamessaging.SQSConnection;

/*
 * Mock test configuration for AWS external dependencies
 */
@Configuration
@Profile("test")
public class MockAwsSqsConfig {

    @Bean
    public SQSConnection getSqsConnection() throws JMSException {
        return mock(SQSConnection.class);
    }

    @Bean
    public MessageConsumer getSqsMessageConsumer(SQSConnection connection) throws JMSException {
        return mock(MessageConsumer.class);
    }

}
