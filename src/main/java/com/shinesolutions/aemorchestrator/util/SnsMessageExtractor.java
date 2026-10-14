package com.shinesolutions.aemorchestrator.util;

import com.shinesolutions.aemorchestrator.model.SnsMessage;
import org.springframework.stereotype.Component;

/** Deserializes an SQS message body into an {@link SnsMessage}. */
@Component
public class SnsMessageExtractor extends MessageExtractor<SnsMessage> {

  /** Creates an extractor for {@link SnsMessage}. */
  public SnsMessageExtractor() {
    super(SnsMessage.class);
  }
}
