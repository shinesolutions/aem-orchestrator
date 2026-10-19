package com.shinesolutions.aemorchestrator.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shinesolutions.aemorchestrator.actions.Action;
import com.shinesolutions.aemorchestrator.model.EventMessage;
import com.shinesolutions.aemorchestrator.util.EventMessageExtractor;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutoScalingTerminateEventHandlerTest {

  @Mock private Map<String, Action> scaleDownAutoScaleGroupMappings;

  @Mock private EventMessageExtractor eventMessageExtractor;

  @InjectMocks private AutoScalingTerminateEventHandler handler;

  private Action action;
  private String messageContent;
  private EventMessage message;

  @BeforeEach
  void setUp() throws Exception {
    action = mock(Action.class);
    messageContent = "testMessage";

    message = new EventMessage();
    message.setAutoScalingGroupName("testGroup");
    message.setEc2InstanceId("test-instance");

    when(eventMessageExtractor.extractMessage(messageContent)).thenReturn(message);
  }

  @Test
  void testNoActionFound() throws Exception {
    when(scaleDownAutoScaleGroupMappings.get(anyString())).thenReturn(null);

    boolean result = handler.handleEvent(messageContent);

    assertThat(result, equalTo(false));
  }

  @Test
  void testInstanceHandlesExceptionsGracefully() throws Exception {
    when(scaleDownAutoScaleGroupMappings.get(anyString())).thenThrow(new RuntimeException());

    boolean result = handler.handleEvent(messageContent);

    assertThat(result, equalTo(false));
  }

  @Test
  void testSuccessAndActionDeleteMessage() throws Exception {
    when(scaleDownAutoScaleGroupMappings.get(anyString())).thenReturn(action);
    when(action.execute(message.getEc2InstanceId())).thenReturn(true);

    boolean result = handler.handleEvent(messageContent);

    assertThat(result, equalTo(true));
    verify(action, times(1)).execute(message.getEc2InstanceId());
  }

  @Test
  void testSuccessAndActionKeepMessage() throws Exception {
    when(scaleDownAutoScaleGroupMappings.get(anyString())).thenReturn(action);
    when(action.execute(message.getEc2InstanceId())).thenReturn(false);

    boolean result = handler.handleEvent(messageContent);

    assertThat(result, equalTo(false));
    verify(action, times(1)).execute(message.getEc2InstanceId());
  }
}
