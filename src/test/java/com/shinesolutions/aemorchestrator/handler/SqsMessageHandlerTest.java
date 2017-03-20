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

import java.util.HashMap;
import java.util.Map;

import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.shinesolutions.aemorchestrator.model.SnsMessage;
import com.shinesolutions.aemorchestrator.util.SnsMessageExtractor;

@RunWith(MockitoJUnitRunner.class)
public class SqsMessageHandlerTest {

    private Map<String, MessageHandler> eventTypeHandlerMappings;
    
    @Mock
    private SnsMessageExtractor snsMessageExtractor;

    @InjectMocks
    private SqsMessageHandler sqsMessageHandler;

    private TextMessage testMessage;
    private SnsMessage snsMessage;

    private MessageHandler mockEventHandler1;
    private MessageHandler mockEventHandler2;
    
    private String subject;
    private String messageBody;
    
    private static final String TEXT = "\"text\"";

    @Before
    public void setup() throws Exception {
        subject = "test1Subject";
        messageBody = TEXT.replace("\"", "\\\"");
        
        snsMessage = new SnsMessage();
        snsMessage.setSubject(subject);
        snsMessage.setMessage(messageBody);

        mockEventHandler1 = mock(MessageHandler.class);
        mockEventHandler2 = mock(MessageHandler.class);
        
        eventTypeHandlerMappings = new HashMap<String, MessageHandler>();
        eventTypeHandlerMappings.put("test1", mockEventHandler1);
        eventTypeHandlerMappings.put("test2", mockEventHandler2);

        setField(sqsMessageHandler, "eventTypeHandlerMappings", eventTypeHandlerMappings);
        
        when(snsMessageExtractor.extractMessage(anyString())).thenReturn(snsMessage);
        
        testMessage = mock(TextMessage.class);
        when(testMessage.getText()).thenReturn("anything");
    }

    @Test
    public void testSuccess() throws Exception {
        ArgumentCaptor<String> eventMessageCaptor = ArgumentCaptor.forClass(String.class);

        when(mockEventHandler1.handleEvent(anyString())).thenReturn(true);

        boolean result = sqsMessageHandler.handleMessage(testMessage);

        verify(mockEventHandler1, times(1)).handleEvent(eventMessageCaptor.capture());

        String eventMessage = eventMessageCaptor.getValue();

        assertThat(result, equalTo(true));
        assertThat(eventMessage, equalTo(TEXT));
    }

    @Test
    public void testSuccessWithDifferentSubject() throws Exception {
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
    public void testNoEventHandlerFound() throws Exception {
        snsMessage.setSubject("uknownSubject");

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
