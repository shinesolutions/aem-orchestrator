package com.shinesolutions.aemorchestrator.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import com.shinesolutions.aemorchestrator.actions.AlarmContentHealthCheckAction;
import com.shinesolutions.aemorchestrator.model.AlarmMessage;
import com.shinesolutions.aemorchestrator.model.Dimension;
import com.shinesolutions.aemorchestrator.model.InstanceTags;
import com.shinesolutions.aemorchestrator.model.Trigger;
import com.shinesolutions.aemorchestrator.util.AlarmMessageExtractor;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlarmMessageHandlerTest {

  @Mock private AlarmMessageExtractor alarmMessageExtractor;

  @Mock private AlarmContentHealthCheckAction alarmContentHealthCheckAction;

  @InjectMocks private AlarmMessageHandler alarmMessageHandler;

  private String messageContent;
  private String pairInstanceId;
  private AlarmMessage message;

  @BeforeEach
  void setUp() throws Exception {
    messageContent = "testMessage";
    pairInstanceId = "pairInstanceId";

    message = new AlarmMessage();
    Dimension dimension = new Dimension(InstanceTags.PAIR_INSTANCE_ID.getTagName(), pairInstanceId);
    message.setTrigger(triggerWithDimensions(Arrays.asList(dimension)));

    // This is called across all test cases
    when(alarmMessageExtractor.extractMessage(messageContent)).thenReturn(message);
  }

  @Test
  void testSuccess() {
    when(alarmContentHealthCheckAction.execute(pairInstanceId)).thenReturn(true);

    boolean result = alarmMessageHandler.handleEvent(messageContent);

    assertThat(result, equalTo(true));
  }

  @Test
  void testNoPairInstanceIdTag() {
    message.setTrigger(triggerWithDimensions(Arrays.asList()));

    boolean result = alarmMessageHandler.handleEvent(messageContent);

    assertThat(result, equalTo(false));
  }

  @Test
  void testWithException() {
    message.setTrigger(triggerWithDimensions(null)); // Will throw NullPointerException

    boolean result = alarmMessageHandler.handleEvent(messageContent);

    assertThat(result, equalTo(false));
  }

  private static Trigger triggerWithDimensions(List<Dimension> dimensions) {
    return new Trigger(null, null, null, null, null, null, null, null, 0, 0, 0, dimensions);
  }
}
