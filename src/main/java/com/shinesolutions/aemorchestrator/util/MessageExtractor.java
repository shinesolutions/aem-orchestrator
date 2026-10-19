package com.shinesolutions.aemorchestrator.util;

import java.io.IOException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Deserializes JSON messages into a model type.
 *
 * <p>Uses a mapper with the Jackson 2 defaults that Jackson 3 changed: unknown properties fail
 * unless the model ignores them, nulls are accepted for primitives, and trailing tokens are
 * ignored.
 *
 * @param <T> the model type
 */
public class MessageExtractor<T> {

  final Class<T> typeParameterClass;

  /** Creates an extractor that deserializes into {@code typeParameterClass}. */
  public MessageExtractor(Class<T> typeParameterClass) {
    this.typeParameterClass = typeParameterClass;
  }

  /**
   * Deserializes a JSON message body into the model type.
   *
   * @throws JacksonException if the body is not valid JSON or does not match the model type
   * @throws IOException if the body cannot be read
   */
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
