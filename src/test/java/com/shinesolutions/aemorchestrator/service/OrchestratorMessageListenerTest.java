package com.shinesolutions.aemorchestrator.service;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.Message;
import javax.jms.MessageConsumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazon.sqs.javamessaging.SQSConnection;
import com.shinesolutions.aemorchestrator.handler.SqsMessageHandler;

@RunWith(MockitoJUnitRunner.class)
public class OrchestratorMessageListenerTest {
    
    @Mock
    private SQSConnection connection;

    @Mock
    private MessageConsumer consumer;

    @Mock
    private SqsMessageHandler messageHandler;
    
    @InjectMocks
    private OrchestratorMessageListener messageReceiver;
    
    private Message message;
    
    @Before
    public void setUp() throws Exception {
        message = mock(Message.class);
        when(message.getJMSMessageID()).thenReturn("test1234");
    }
    
    @Test
    public void testReceiveNullMessage() {
        try {
            messageReceiver.onMessage(null);
        } catch (Exception e) {
            fail("Should not throw exception on null message");
        }
    }

    @Test
    public void testRecieveValidMessageButLeaveOnQueue() throws Exception {
        when(messageHandler.handleMessage(message)).thenReturn(false);
        
        messageReceiver.onMessage(message);
        
        verify(message, never()).acknowledge();
        verify(messageHandler, times(1)).handleMessage(message);
    }
    
    @Test
    public void testRecieveValidMessageAndRemovefromQueue() throws Exception {
        when(messageHandler.handleMessage(message)).thenReturn(true);
        
        messageReceiver.onMessage(message);
        
        verify(message, times(1)).acknowledge();
        verify(messageHandler, times(1)).handleMessage(message);
    }
    
    @Test
    public void testRecieveValidMessageHandlerThrowsException() throws Exception {
        when(messageHandler.handleMessage(message)).thenThrow(new RuntimeException("Test"));
        
        try {
            messageReceiver.onMessage(message);
        } catch (Exception e) {
            fail("Should not throw exception if handler throws exception");
        }
        
        verify(message, never()).acknowledge();
        verify(messageHandler, times(1)).handleMessage(message);
    }
    
    @Test
    public void testStart() throws Exception {
        
        messageReceiver.start();
        
        verify(consumer, times(1)).setMessageListener(messageReceiver);
        verify(connection, times(1)).start();
    }
    
    @Test
    public void testCleanUp() throws Exception {
        
        messageReceiver.cleanUp();
        
        verify(connection, times(1)).stop();
    }

}
