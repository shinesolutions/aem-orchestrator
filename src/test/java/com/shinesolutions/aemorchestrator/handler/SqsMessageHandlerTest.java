package com.shinesolutions.aemorchestrator.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.shinesolutions.aemorchestrator.model.SnsMessage;
import com.shinesolutions.aemorchestrator.util.SnsMessageExtractor;

@RunWith(MockitoJUnitRunner.class)
public class SqsMessageHandlerTest {

    @Mock
    private SnsMessageExtractor snsMessageExtractor;

    @InjectMocks
    private SqsMessageHandler sqsMessageHandler;

    private TextMessage testMessage;
    private SnsMessage snsMessage;

    private MessageHandler mockEventHandler1;
    private MessageHandler mockEventHandler2;
    
    private static final String TEXT = "\"text\"";

    @Before
    public void setup() throws Exception {
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
    public void testSuccess() {
        ArgumentCaptor<String> eventMessageCaptor = ArgumentCaptor.forClass(String.class);

        when(mockEventHandler1.handleEvent(anyString())).thenReturn(true);

        boolean result = sqsMessageHandler.handleMessage(testMessage);

        verify(mockEventHandler1, times(1)).handleEvent(eventMessageCaptor.capture());

        String eventMessage = eventMessageCaptor.getValue();

        assertThat(result, equalTo(true));
        assertThat(eventMessage, equalTo(TEXT));
    }

    @Test
    public void testSuccessWithDifferentSubject() {
        snsMessage.setSubject("test2Subject");
        
        when(mockEventHandler2.handleEvent(anyString())).thenReturn(true);

        boolean result = sqsMessageHandler.handleMessage(testMessage);

        verify(mockEventHandler1, never()).handleEvent(anyString());
        verify(mockEventHandler2, times(1)).handleEvent(anyString());

        assertThat(result, equalTo(true));
    }
    
    @Test
    public void testErrorWhenReadingMessage() throws Exception {
        when(snsMessageExtractor.extractMessage(anyString())).thenThrow(new RuntimeException());

        boolean result = sqsMessageHandler.handleMessage(testMessage);

        verify(mockEventHandler1, never()).handleEvent(anyString());
        verify(mockEventHandler2, never()).handleEvent(anyString());

        assertThat(result, equalTo(true));
    }

    @Test
    public void testNoSnsMessage() throws IOException {
        snsMessage = null;
        when(snsMessageExtractor.extractMessage(anyString())).thenReturn(snsMessage);
        
    
        boolean result = sqsMessageHandler.handleMessage(testMessage);
    
        verify(mockEventHandler1, never()).handleEvent(anyString());
        verify(mockEventHandler2, never()).handleEvent(anyString());
    
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testNoSnsMessageSubject() {
        snsMessage.setSubject(null);

        boolean result = sqsMessageHandler.handleMessage(testMessage);

        verify(mockEventHandler1, never()).handleEvent(anyString());
        verify(mockEventHandler2, never()).handleEvent(anyString());

        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testNoEventHandlerFound() {
        snsMessage.setSubject("unknownSubject");

        boolean result = sqsMessageHandler.handleMessage(testMessage);

        verify(mockEventHandler1, never()).handleEvent(anyString());
        verify(mockEventHandler2, never()).handleEvent(anyString());

        //Ensure that it deletes unknown messages from the queue
        assertThat(result, equalTo(true));
    }

    @Test
    public void testEventHandlerError() {
        doThrow(new RuntimeException("Test exception")).when(mockEventHandler1).handleEvent(anyString());

        boolean result = sqsMessageHandler.handleMessage(testMessage);

        verify(mockEventHandler1, times(1)).handleEvent(anyString());

        assertThat(result, equalTo(false));
    }
}
