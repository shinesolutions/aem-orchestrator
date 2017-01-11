package com.shinesolutions.aemorchestrator.service;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinesolutions.aemorchestrator.actions.AemAction;
import com.shinesolutions.aemorchestrator.model.AemOrchestratorMessage;

/*
 * Invokes the correct action based on the message type
 */
@Component
public class CommandInvoker implements SqsMessageHandler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private Map<String, String> messageTypeActionMapping;

	public void handleMessgae(Message message) {

		AemOrchestratorMessage aemOrchestratorMessage = null;
		try {
			aemOrchestratorMessage = convertMessage(message.getBody());
		} catch (Exception e) {
			logger.error("Error when reading message body", e);
		}

		if (aemOrchestratorMessage != null) {
			String messageType = aemOrchestratorMessage.getEvent();// message.getJMSType();

			// Get class mapping for message type:
			String actionClassName = messageTypeActionMapping.get(messageType);

			if (actionClassName == null || actionClassName.isEmpty()) {
				logger.error("No action class found for message type: " + messageType);
			} else {
				AemAction action = null;

				try {
					action = (AemAction) (Class.forName(actionClassName)).newInstance();
					logger.debug("Executing action: " + actionClassName);
					action.execute(message);
				} catch (Throwable e) {
					logger.error(
							"Failed to invoke action class (" + actionClassName + ") for message type: " + messageType,
							e);
				}
			}
		}

	}

	private AemOrchestratorMessage convertMessage(String sqsMessageBody)
			throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		AemOrchestratorMessage aom = mapper.readValue(sqsMessageBody, AemOrchestratorMessage.class);

		return aom;
	}

}
