package com.shinesolutions.aemorchestrator.util;

import com.shinesolutions.aemorchestrator.model.AlarmMessage;
import org.springframework.stereotype.Component;

/** Deserializes CloudWatch alarm JSON into an {@link AlarmMessage}. */
@Component
public class AlarmMessageExtractor extends MessageExtractor<AlarmMessage> {

  /** Creates an extractor for {@link AlarmMessage}. */
  public AlarmMessageExtractor() {
    super(AlarmMessage.class);
  }
}
