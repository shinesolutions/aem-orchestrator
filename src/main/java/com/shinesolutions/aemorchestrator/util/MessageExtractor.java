package com.shinesolutions.aemorchestrator.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageExtractor<T> {

    final Class<T> typeParameterClass;

    public MessageExtractor(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
    }

    public T extractMessage(String sqsMessageBody) throws JsonParseException, JsonMappingException, IOException {
        
        ObjectMapper eventMapper = new ObjectMapper();
        T message = eventMapper.readValue(sqsMessageBody, typeParameterClass);

        return message;
    }

}
