package com.shinesolutions.aemorchestrator.handler;

import java.util.Map;

import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.exception.MessageHandlerNotFound;
import com.shinesolutions.aemorchestrator.model.SnsMessage;
import com.shinesolutions.aemorchestrator.util.SnsMessageExtractor;

/*
 * Invokes the correct action based on the message type (taken from subject)
 */
@Component
public class SqsMessageHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private Map<String, MessageHandler> eventTypeHandlerMappings;
    
    @Resource
    private SnsMessageExtractor snsMessageExtractor;

    public boolean handleMessage(Message message) {
        
        boolean deleteMessage = false;
        SnsMessage snsMessage = null;
        
        try {
            String messageBody = ((TextMessage)message).getText();
            logger.debug("Raw message body: " + messageBody); 
                
            snsMessage = snsMessageExtractor.extractMessage(messageBody);
            
        } catch (Exception e) {
            logger.error("Error when attempting to read message", e);
            deleteMessage = true;
        }

        if(snsMessage != null && snsMessage.getSubject() != null) {
            // Get class mapping for message type:
            MessageHandler eventHandler = null;
            try {
                eventHandler = getHandler(snsMessage.getSubject());
            } catch (MessageHandlerNotFound e) {
                logger.error("Failed to find message handler", e);
            }
    
            if (eventHandler != null) {
                try {
                    logger.debug("Handling event for subject: " + snsMessage.getSubject());
                    deleteMessage = eventHandler.handleEvent(prepareMessageBody(snsMessage.getMessage()));
                    
                } catch (Exception e) {
                    logger.error("Failed to handle event for message with subject: " + snsMessage.getSubject(), e);
                }
            } else {
                deleteMessage = true;
            }
        }

        return deleteMessage;
    }
    
    private MessageHandler getHandler(String msgSubject) throws MessageHandlerNotFound {
        String key = eventTypeHandlerMappings.keySet().stream().filter(
            m -> msgSubject.startsWith(m)).findFirst().orElseThrow(() -> new MessageHandlerNotFound(msgSubject));
        logger.debug("Using Message Handler for key: " + key);
        return eventTypeHandlerMappings.get(key);
    }
    
    private String prepareMessageBody(String messageBody) {
        // Body contains \" instead of just ". Need to replace before attempting to map to object
        return messageBody.replace("\\\"", "\"");
    }

}
