package com.shinesolutions.aemorchestrator.service;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazon.sqs.javamessaging.SQSConnection;
import com.shinesolutions.aemorchestrator.handler.MessageHandler;

/*
 * Polls the SQS queue checking for messages. If a message is found, then pass it to a message handler
 */
@Component
public class MessageReceiver implements Runnable {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private SQSConnection connection;

	@Resource
	private MessageConsumer consumer;

	@Resource
	private MessageHandler messageHandler;

	@Override
	public void run() {
		try {
			do {
				logger.debug("Waiting for messages");
				
				Message message = consumer.receive(TimeUnit.SECONDS.toMillis(20));

				if (message != null) {
					logger.info("Message received with id: " + message.getJMSMessageID());

					boolean removeMessageFromQueue = messageHandler.handleMessage(message);
					
					//Acknowledging the message with remove it from the queue
					if(removeMessageFromQueue) {
					    message.acknowledge();
					    logger.info("Acknowledged message " + message.getJMSMessageID());
					}
				} else {
					logger.info("No messages received, retrying");
				}

			} while(!Thread.interrupted());
		} catch (JMSException e) {
			logger.error("Error receiving from SQS: ", e);
		}
	}

	@PostConstruct
	public void initIt() throws Exception {
		logger.debug("Initialising message receiver, starting SQS connection");
		connection.start();
	}

	@PreDestroy
	public void cleanUp() throws Exception {
		logger.debug("Destroying message receiver, stopping SQS connection");
		connection.stop();
	}

}
