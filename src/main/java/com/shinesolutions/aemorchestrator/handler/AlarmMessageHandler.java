package com.shinesolutions.aemorchestrator.handler;

import com.shinesolutions.aemorchestrator.actions.AlarmContentHealthCheckAction;
import com.shinesolutions.aemorchestrator.model.AlarmMessage;
import com.shinesolutions.aemorchestrator.model.Dimension;
import com.shinesolutions.aemorchestrator.model.InstanceTags;
import com.shinesolutions.aemorchestrator.util.AlarmMessageExtractor;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AlarmMessageHandler implements MessageHandler {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource private AlarmMessageExtractor alarmMessageExtractor;

  @Resource private AlarmContentHealthCheckAction alarmContentHealthCheckAction;

  @Override
  public boolean handleEvent(String message) {
    logger.debug("Raw message: " + message);
    boolean success = false;

    try {
      AlarmMessage alarmMessage = alarmMessageExtractor.extractMessage(message);
      List<Dimension> dimensions = alarmMessage.getTrigger().getDimensions();

      Map<String, String> dimentionMap =
          dimensions.stream().collect(Collectors.toMap(Dimension::getName, Dimension::getValue));

      String pairInstanceId = dimentionMap.get(InstanceTags.PAIR_INSTANCE_ID.getTagName());
      if (pairInstanceId != null) {
        success = alarmContentHealthCheckAction.execute(pairInstanceId);
      } else {
        logger.warn(
            "Alarm message is missing "
                + InstanceTags.PAIR_INSTANCE_ID.getTagName()
                + " tag."
                + " Unable to process");
      }

    } catch (Exception e) {
      logger.error("Failed to execute 'alarm' action", e);
    }

    return success;
  }
}
