package com.shinesolutions.aemorchestrator.util;

import com.shinesolutions.aemorchestrator.model.AlarmMessage;
import org.springframework.stereotype.Component;

@Component
public class AlarmMessageExtractor extends MessageExtractor<AlarmMessage> {

  public AlarmMessageExtractor() {
    super(AlarmMessage.class);
  }
}
