package com.shinesolutions.aemorchestrator.util;

import com.shinesolutions.aemorchestrator.model.EventMessage;
import org.springframework.stereotype.Component;

@Component
public class EventMessageExtractor extends MessageExtractor<EventMessage> {

  public EventMessageExtractor() {
    super(EventMessage.class);
  }
}
