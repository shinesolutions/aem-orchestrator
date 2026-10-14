package com.shinesolutions.aemorchestrator.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shinesolutions.aemorchestrator.actions.Action;
import com.shinesolutions.aemorchestrator.model.EventMessage;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import com.shinesolutions.aemorchestrator.util.EventMessageExtractor;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutoScalingLaunchEventHandlerTest {

  @Mock private Map<String, Action> scaleUpAutoScaleGroupMappings;

  @Mock private AwsHelperService awsHelperService;

  @Mock private EventMessageExtractor eventMessageExtractor;

  @InjectMocks private AutoScalingLaunchEventHandler handler;

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
  void testNoActionToPerform() {
    message.setDescription("Moving EC2 instance out of Standby");

    boolean result = handler.handleEvent(messageContent);

    assertThat(result, equalTo(true));
  }

  @Test
  void testNoActionFound() {
    when(scaleUpAutoScaleGroupMappings.get(anyString())).thenReturn(null);

    boolean result = handler.handleEvent(messageContent);

    assertThat(result, equalTo(false));
  }

  @Test
  void testNoActionFoundWithEventDescription() {
    message.setDescription("Test description");
    when(scaleUpAutoScaleGroupMappings.get(anyString())).thenReturn(null);

    boolean result = handler.handleEvent(messageContent);

    assertThat(result, equalTo(false));
  }

  @Test
  void testInstanceNotRunning() {
    when(scaleUpAutoScaleGroupMappings.get(anyString())).thenReturn(action);
    when(awsHelperService.isInstanceRunning(message.getEc2InstanceId())).thenReturn(false);

    boolean result = handler.handleEvent(messageContent);

    assertThat(result, equalTo(true));
    verify(action, never()).execute(message.getEc2InstanceId());
  }

  @Test
  void testInstanceHandlesExceptionsGracefully() {
    when(scaleUpAutoScaleGroupMappings.get(anyString())).thenThrow(new RuntimeException());

    boolean result = handler.handleEvent(messageContent);

    assertThat(result, equalTo(false));
  }

  @Test
  void testSuccessAndActionDeleteMessage() {
    when(scaleUpAutoScaleGroupMappings.get(anyString())).thenReturn(action);
    when(awsHelperService.isInstanceRunning(message.getEc2InstanceId())).thenReturn(true);
    when(action.execute(message.getEc2InstanceId())).thenReturn(true);

    boolean result = handler.handleEvent(messageContent);

    assertThat(result, equalTo(true));
    verify(action, times(1)).execute(message.getEc2InstanceId());
  }

  @Test
  void testSuccessAndActionKeepMessage() {
    when(scaleUpAutoScaleGroupMappings.get(anyString())).thenReturn(action);
    when(awsHelperService.isInstanceRunning(message.getEc2InstanceId())).thenReturn(true);
    when(action.execute(message.getEc2InstanceId())).thenReturn(false);

    boolean result = handler.handleEvent(messageContent);

    assertThat(result, equalTo(false));
    verify(action, times(1)).execute(message.getEc2InstanceId());
  }
}
