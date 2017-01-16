package com.shinesolutions.aemorchestrator.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinesolutions.aemorchestrator.model.EventMessage;

public class MessageExtractor {

    public static EventMessage extractEventMessage(String sqsMessageBody)
        throws JsonParseException, JsonMappingException, IOException {
        // Body contains \" instead of just ". Need to replace before attempting
        // to map to object
        String preparedBody = sqsMessageBody.replace("\\\"", "\"");

        ObjectMapper mapper = new ObjectMapper();
        EventMessage eventMsg = mapper.readValue(preparedBody, EventMessage.class);

        return eventMsg;
    }

}
