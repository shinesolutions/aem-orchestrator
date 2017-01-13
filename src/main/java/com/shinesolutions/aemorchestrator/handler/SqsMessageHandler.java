package com.shinesolutions.aemorchestrator.handler;

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
import com.shinesolutions.aemorchestrator.model.EventMessage;

/*
 * Invokes the correct action based on the message type
 */
@Component
public class SqsMessageHandler implements MessageHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private Map<String, EventHandler> eventTypeHandlerMappings;

    public boolean handleMessage(Message message) {
        boolean handleSuccess = false;
        EventMessage eventMessage = null;
        try {
            eventMessage = extractMessageBody(message.getBody());
        } catch (Exception e) {
            logger.error("Error when reading message body, event will not be handled", e);
        }

        if (eventMessage != null) {
            String eventType = eventMessage.getEvent();

            // Get class mapping for message type:
            EventHandler eventHandler = eventTypeHandlerMappings.get(eventType);

            if (eventHandler == null) {
                logger.error("No event handler found for message type: " + eventType);
            } else {
                try {
                    logger.debug("Handling event: " + eventType);
                    handleSuccess = eventHandler.handleEvent(eventMessage);
                    
                } catch (Exception e) {
                    logger.error("Failed to handle event for message type: " + eventType, e);
                }
            }
        }

        return handleSuccess;
    }

    private EventMessage extractMessageBody(String sqsMessageBody)
        throws JsonParseException, JsonMappingException, IOException {

        // Body contains \" instead of just ". Need to replace before attempting
        // to map to object
        String preparedBody = sqsMessageBody.replace("\\\"", "\"");

        ObjectMapper mapper = new ObjectMapper();
        EventMessage eventMsg = mapper.readValue(preparedBody, EventMessage.class);

        return eventMsg;
    }

}
