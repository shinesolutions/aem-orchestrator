package com.shinesolutions.aemorchestrator.util;

import java.io.IOException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;

public class MessageExtractor<T> {

  final Class<T> typeParameterClass;

  public MessageExtractor(Class<T> typeParameterClass) {
    this.typeParameterClass = typeParameterClass;
  }

  public T extractMessage(String sqsMessageBody)
      throws JacksonException, DatabindException, IOException {

    ObjectMapper eventMapper = new ObjectMapper();
    return eventMapper.readValue(sqsMessageBody, typeParameterClass);
  }
}
