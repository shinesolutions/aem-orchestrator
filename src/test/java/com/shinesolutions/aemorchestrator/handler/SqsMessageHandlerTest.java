package com.shinesolutions.aemorchestrator.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.shinesolutions.aemorchestrator.model.SnsMessage;
import com.shinesolutions.aemorchestrator.util.SnsMessageExtractor;
import jakarta.jms.TextMessage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SqsMessageHandlerTest {

  @Mock private SnsMessageExtractor snsMessageExtractor;

  @InjectMocks private SqsMessageHandler sqsMessageHandler;

  private TextMessage testMessage;
  private SnsMessage snsMessage;

  private MessageHandler mockEventHandler1;
  private MessageHandler mockEventHandler2;

  private static final String TEXT = "\"text\"";

  @BeforeEach
  void setup() throws Exception {
    String subject = "test1Subject";
    String messageBody = TEXT.replace("\"", "\\\"");

    snsMessage = new SnsMessage();
    snsMessage.setSubject(subject);
    snsMessage.setMessage(messageBody);

    mockEventHandler1 = mock(MessageHandler.class);
    mockEventHandler2 = mock(MessageHandler.class);

    Map<String, MessageHandler> eventTypeHandlerMappings = new HashMap<>();
    eventTypeHandlerMappings.put("test1", mockEventHandler1);
    eventTypeHandlerMappings.put("test2", mockEventHandler2);

    setField(sqsMessageHandler, "eventTypeHandlerMappings", eventTypeHandlerMappings);

    when(snsMessageExtractor.extractMessage(anyString())).thenReturn(snsMessage);

    testMessage = mock(TextMessage.class);
    when(testMessage.getText()).thenReturn("anything");
  }

  @Test
  void testSuccess() {
    ArgumentCaptor<String> eventMessageCaptor = ArgumentCaptor.forClass(String.class);

    when(mockEventHandler1.handleEvent(anyString())).thenReturn(true);

    boolean result = sqsMessageHandler.handleMessage(testMessage);

    verify(mockEventHandler1, times(1)).handleEvent(eventMessageCaptor.capture());

    String eventMessage = eventMessageCaptor.getValue();

    assertThat(result, equalTo(true));
    assertThat(eventMessage, equalTo(TEXT));
  }

  @Test
  void testSuccessWithDifferentSubject() {
    snsMessage.setSubject("test2Subject");

    when(mockEventHandler2.handleEvent(anyString())).thenReturn(true);

    boolean result = sqsMessageHandler.handleMessage(testMessage);

    verify(mockEventHandler1, never()).handleEvent(anyString());
    verify(mockEventHandler2, times(1)).handleEvent(anyString());

    assertThat(result, equalTo(true));
  }

  @Test
  void testErrorWhenReadingMessage() throws Exception {
    when(snsMessageExtractor.extractMessage(anyString())).thenThrow(new RuntimeException());

    boolean result = sqsMessageHandler.handleMessage(testMessage);

    verify(mockEventHandler1, never()).handleEvent(anyString());
    verify(mockEventHandler2, never()).handleEvent(anyString());

    assertThat(result, equalTo(true));
  }

  @Test
  void testNoSnsMessage() throws IOException {
    snsMessage = null;
    when(snsMessageExtractor.extractMessage(anyString())).thenReturn(snsMessage);

    boolean result = sqsMessageHandler.handleMessage(testMessage);

    verify(mockEventHandler1, never()).handleEvent(anyString());
    verify(mockEventHandler2, never()).handleEvent(anyString());

    assertThat(result, equalTo(false));
  }

  @Test
  void testNoSnsMessageSubject() {
    snsMessage.setSubject(null);

    boolean result = sqsMessageHandler.handleMessage(testMessage);

    verify(mockEventHandler1, never()).handleEvent(anyString());
    verify(mockEventHandler2, never()).handleEvent(anyString());

    assertThat(result, equalTo(false));
  }

  @Test
  void testNoEventHandlerFound() {
    snsMessage.setSubject("unknownSubject");

    boolean result = sqsMessageHandler.handleMessage(testMessage);

    verify(mockEventHandler1, never()).handleEvent(anyString());
    verify(mockEventHandler2, never()).handleEvent(anyString());

    // Ensure that it deletes unknown messages from the queue
    assertThat(result, equalTo(true));
  }

  @Test
  void testEventHandlerError() {
    doThrow(new RuntimeException("Test exception"))
        .when(mockEventHandler1)
        .handleEvent(anyString());

    boolean result = sqsMessageHandler.handleMessage(testMessage);

    verify(mockEventHandler1, times(1)).handleEvent(anyString());

    assertThat(result, equalTo(false));
  }
}
