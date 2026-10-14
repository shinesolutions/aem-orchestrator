package com.shinesolutions.aemorchestrator.util;

import com.shinesolutions.aemorchestrator.model.EventMessage;
import org.springframework.stereotype.Component;

/** Deserializes Auto Scaling event JSON into an {@link EventMessage}. */
@Component
public class EventMessageExtractor extends MessageExtractor<EventMessage> {

  /** Creates an extractor for {@link EventMessage}. */
  public EventMessageExtractor() {
    super(EventMessage.class);
  }
}
