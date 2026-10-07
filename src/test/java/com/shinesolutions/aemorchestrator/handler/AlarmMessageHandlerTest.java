package com.shinesolutions.aemorchestrator.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.shinesolutions.aemorchestrator.actions.AlarmContentHealthCheckAction;
import com.shinesolutions.aemorchestrator.model.AlarmMessage;
import com.shinesolutions.aemorchestrator.model.Dimension;
import com.shinesolutions.aemorchestrator.model.InstanceTags;
import com.shinesolutions.aemorchestrator.model.Trigger;
import com.shinesolutions.aemorchestrator.util.AlarmMessageExtractor;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AlarmMessageHandlerTest {

  @Mock private AlarmMessageExtractor alarmMessageExtractor;

  @Mock private AlarmContentHealthCheckAction alarmContentHealthCheckAction;

  @InjectMocks private AlarmMessageHandler alarmMessageHandler;

  private String messageContent;
  private String pairInstanceId;
  private AlarmMessage message;

  @BeforeEach
  public void setUp() throws Exception {
    messageContent = "testMessage";
    pairInstanceId = "pairInstanceId";

    message = new AlarmMessage();
    Trigger trigger = new Trigger();
    Dimension dimension = new Dimension();
    dimension.setName(InstanceTags.PAIR_INSTANCE_ID.getTagName());
    dimension.setValue(pairInstanceId);
    trigger.setDimensions(Arrays.asList(dimension));
    message.setTrigger(trigger);

    // This is called across all test cases
    when(alarmMessageExtractor.extractMessage(messageContent)).thenReturn(message);
    lenient().when(alarmContentHealthCheckAction.execute(pairInstanceId)).thenReturn(true);
  }

  @Test
  public void testSuccess() {
    boolean result = alarmMessageHandler.handleEvent(messageContent);

    assertThat(result, equalTo(true));
  }

  @Test
  public void testNoPairInstanceIdTag() {
    message.getTrigger().setDimensions(Arrays.asList());

    boolean result = alarmMessageHandler.handleEvent(messageContent);

    assertThat(result, equalTo(false));
  }

  @Test
  public void testWithException() {
    message.getTrigger().setDimensions(null); // Will throw NullPointerException

    boolean result = alarmMessageHandler.handleEvent(messageContent);

    assertThat(result, equalTo(false));
  }
}
