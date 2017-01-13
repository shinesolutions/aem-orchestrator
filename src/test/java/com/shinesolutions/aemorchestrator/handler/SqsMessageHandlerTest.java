package com.shinesolutions.aemorchestrator.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.services.sqs.model.Message;
import com.shinesolutions.aemorchestrator.handler.EventHandler;
import com.shinesolutions.aemorchestrator.handler.SqsMessageHandler;
import com.shinesolutions.aemorchestrator.model.EventMessage;

@RunWith(MockitoJUnitRunner.class)
public class SqsMessageHandlerTest {

    @Mock
    private Map<String, EventHandler> eventTypeHandlerMappings;

    @InjectMocks
    private SqsMessageHandler sqsMessageHandler;

    private Message testMessage;

    private EventHandler mockEventHandler;

    @Before
    @SuppressWarnings("resource")
    public void setup() throws Exception {
        // Read in file containing test example of message body
        File sampleFile = new File(getClass().getResource("/sample-sqs-message-body-raw.txt").getFile());
        String sampleFileContent = new Scanner(sampleFile).useDelimiter("\\Z").next();

        testMessage = new Message();
        testMessage.setBody(sampleFileContent);

        mockEventHandler = mock(EventHandler.class);

        when(eventTypeHandlerMappings.get("autoscaling:EC2_INSTANCE_TERMINATE")).thenReturn(mockEventHandler);
    }

    @Test
    public void testSuccess() {
        ArgumentCaptor<EventMessage> eventMessageCaptor = ArgumentCaptor.forClass(EventMessage.class);
        
        when(mockEventHandler.handleEvent(any(EventMessage.class))).thenReturn(true);
        
        boolean result = sqsMessageHandler.handleMessage(testMessage);

        verify(mockEventHandler, times(1)).handleEvent(eventMessageCaptor.capture());

        EventMessage eventMessage = eventMessageCaptor.getValue();

        assertThat(result, equalTo(true));
        assertThat(eventMessage.getActivityId(), equalTo("d9acbc52-51c1-1175-a339-a472d0222a98"));
    }

    @Test
    public void testBadMessageBody() {
        testMessage.setBody("An invalid string");
        boolean result = sqsMessageHandler.handleMessage(testMessage);

        verify(mockEventHandler, never()).handleEvent(any(EventMessage.class));

        assertThat(result, equalTo(false));
    }

    @Test
    public void testNoEventHandlerFound() {
        when(eventTypeHandlerMappings.get("autoscaling:EC2_INSTANCE_TERMINATE")).thenReturn(null);

        boolean result = sqsMessageHandler.handleMessage(testMessage);

        verify(mockEventHandler, never()).handleEvent(any(EventMessage.class));

        assertThat(result, equalTo(false));
    }

    @Test
    public void testEventHandlerError() {
        doThrow(new RuntimeException("Test exception")).when(mockEventHandler).handleEvent(any(EventMessage.class));

        boolean result = sqsMessageHandler.handleMessage(testMessage);

        verify(mockEventHandler, times(1)).handleEvent(any(EventMessage.class));

        assertThat(result, equalTo(false));
    }

}
