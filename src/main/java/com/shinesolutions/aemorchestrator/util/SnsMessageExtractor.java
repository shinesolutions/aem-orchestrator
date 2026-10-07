package com.shinesolutions.aemorchestrator.util;

import com.shinesolutions.aemorchestrator.model.SnsMessage;
import org.springframework.stereotype.Component;

@Component
public class SnsMessageExtractor extends MessageExtractor<SnsMessage> {

  public SnsMessageExtractor() {
    super(SnsMessage.class);
  }
}
