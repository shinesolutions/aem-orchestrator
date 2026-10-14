package com.shinesolutions.aemorchestrator.util;

import java.io.IOException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public class MessageExtractor<T> {

  final Class<T> typeParameterClass;

  public MessageExtractor(Class<T> typeParameterClass) {
    this.typeParameterClass = typeParameterClass;
  }

  public T extractMessage(String sqsMessageBody)
      throws JacksonException, DatabindException, IOException {

    ObjectMapper eventMapper = newObjectMapper();
    return eventMapper.readValue(sqsMessageBody, typeParameterClass);
  }

  /** ObjectMapper with the Jackson 2 deserialization defaults that Jackson 3 changed. */
  static ObjectMapper newObjectMapper() {
    return JsonMapper.builder()
        .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
        .build();
  }
}
